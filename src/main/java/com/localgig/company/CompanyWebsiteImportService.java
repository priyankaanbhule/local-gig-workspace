package com.localgig.company;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

@Service
public class CompanyWebsiteImportService {
    private static final int MAX_PAGE_BYTES = 1_000_000;

    public CompanyWebsiteImportResponse importFrom(String rawWebsite) {
        URI website = parsePublicHttpsWebsite(rawWebsite);
        validatePublicHost(website.getHost());

        final Document document;
        try {
            Connection.Response response = Jsoup.connect(website.toString())
                    .userAgent("LocalGigCompanyProfileImporter/1.0")
                    .timeout(6_000)
                    .maxBodySize(MAX_PAGE_BYTES)
                    .followRedirects(false)
                    .ignoreHttpErrors(true)
                    .execute();

            if (response.statusCode() >= 300 && response.statusCode() < 400) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "That website redirects. Enter its final HTTPS URL instead.");
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The company website could not be read. You can still enter its details manually.");
            }
            document = response.parse();
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Could not import that website. Check the HTTPS URL or enter company details manually.");
        }

        String description = firstNonBlank(
                meta(document, "meta[property='og:description']"),
                meta(document, "meta[name=description]"),
                meta(document, "meta[name=twitter:description]"));
        String companyName = firstNonBlank(
                meta(document, "meta[property='og:site_name']"),
                meta(document, "meta[name=application-name]"),
                document.title(),
                nameFromHost(website.getHost()));

        return new CompanyWebsiteImportResponse(
                limit(companyName, 160),
                limit(firstNonBlank(meta(document, "meta[property='og:title']"), description), 180),
                limit(description, 3_000),
                website.toString(),
                findLinkedInCompanyUrl(document),
                "",
                "",
                findLogoUrl(document, website),
                "");
    }

    private URI parsePublicHttpsWebsite(String rawWebsite) {
        try {
            URI uri = new URI(rawWebsite == null ? "" : rawWebsite.trim()).normalize();
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) || host == null
                    || uri.getUserInfo() != null || (uri.getPort() != -1 && uri.getPort() != 443)
                    || host.indexOf('.') < 1 || host.endsWith(".")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Enter a public HTTPS company website URL, such as https://example.com.");
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a valid public HTTPS company website URL.");
        }
    }

    private void validatePublicHost(String host) {
        try {
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (isNonPublicAddress(address)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Company import only supports publicly reachable websites.");
                }
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The company website host could not be resolved.");
        }
    }

    private boolean isNonPublicAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        if (address instanceof Inet4Address) {
            byte[] bytes = address.getAddress();
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            return first == 0 || first >= 224 || (first == 100 && second >= 64 && second <= 127);
        }
        byte[] bytes = address.getAddress();
        int first = Byte.toUnsignedInt(bytes[0]);
        int second = Byte.toUnsignedInt(bytes[1]);
        return (first & 0xFE) == 0xFC || (first == 0xFE && (second & 0xC0) == 0x80);
    }

    private String meta(Document document, String selector) {
        Element element = document.selectFirst(selector);
        return element == null ? "" : element.attr("content").trim();
    }

    private String findLinkedInCompanyUrl(Document document) {
        for (Element link : document.select("a[href]")) {
            String href = link.absUrl("href");
            if (href.isBlank()) continue;
            try {
                URI uri = URI.create(href);
                String host = uri.getHost();
                if ("https".equalsIgnoreCase(uri.getScheme()) && host != null
                        && (host.equalsIgnoreCase("linkedin.com") || host.toLowerCase(Locale.ROOT).endsWith(".linkedin.com"))
                        && uri.getPath() != null && uri.getPath().toLowerCase(Locale.ROOT).startsWith("/company/")) {
                    return limit(uri.toString(), 500);
                }
            } catch (IllegalArgumentException ignored) {
                // Skip malformed social links and continue parsing the public page.
            }
        }
        return "";
    }

    private String findLogoUrl(Document document, URI base) {
        String candidate = firstNonBlank(
                meta(document, "meta[property='og:image']"),
                meta(document, "meta[name=twitter:image]"));
        if (candidate.isBlank()) {
            Element icon = document.select("link[rel][href]").stream()
                    .filter(link -> Arrays.stream(link.attr("rel").toLowerCase(Locale.ROOT).split("\\s+"))
                            .anyMatch(value -> value.contains("icon")))
                    .findFirst().orElse(null);
            candidate = icon == null ? "" : icon.absUrl("href");
        }
        if (candidate.isBlank()) return "";
        try {
            URI resolved = base.resolve(candidate).normalize();
            return "https".equalsIgnoreCase(resolved.getScheme()) && resolved.getHost() != null
                    ? limit(resolved.toString(), 500) : "";
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private String nameFromHost(String host) {
        String name = host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "")
                .split("\\.")[0].replace('-', ' ').replace('_', ' ');
        return Arrays.stream(name.split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1))
                .reduce((left, right) -> left + " " + right).orElse("Company");
    }

    private String firstNonBlank(String... values) {
        return Arrays.stream(values).filter(value -> value != null && !value.isBlank())
                .map(String::trim).findFirst().orElse("");
    }

    private String limit(String value, int maxLength) {
        if (value == null) return "";
        String clean = value.trim();
        return clean.length() <= maxLength ? clean : clean.substring(0, maxLength);
    }
}

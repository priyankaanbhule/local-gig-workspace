package com.localgig.common.upload;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
    private final Path uploadDirectory;

    public FileUploadController(@Value("${localgig.upload-dir:uploads}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@AuthenticationPrincipal Object user,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam String category) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to upload a file");
        if (file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a file first");
        if (file.getSize() > 5 * 1024 * 1024) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be 5 MB or smaller");

        String original = file.getOriginalFilename() == null ? "upload" : Path.of(file.getOriginalFilename()).getFileName().toString();
        String extension = extension(original);
        String type = category.trim().toUpperCase(Locale.ROOT);
        boolean image = type.equals("PROFILE_IMAGE") && java.util.Set.of("png", "jpg", "jpeg", "webp").contains(extension);
        boolean resume = type.equals("RESUME") && java.util.Set.of("pdf", "doc", "docx").contains(extension);
        if (!image && !resume) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Profile pictures must be PNG, JPG, or WEBP; resumes must be PDF, DOC, or DOCX");
        if (image && file.getSize() > 2 * 1024 * 1024)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile pictures must be 2 MB or smaller");

        String storedName = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(uploadDirectory);
            Path target = uploadDirectory.resolve(storedName).normalize();
            if (!target.startsWith(uploadDirectory)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
            file.transferTo(target);
            return new UploadResponse("/api/files/" + storedName, original, file.getSize());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File could not be saved");
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal Object user, @PathVariable String filename) {
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to access this file");
        if (!filename.matches("[a-f0-9-]{36}\\.(pdf|doc|docx|png|jpg|jpeg|webp)"))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        Path path = uploadDirectory.resolve(filename).normalize();
        if (!path.startsWith(uploadDirectory) || !Files.isRegularFile(path))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        String extension = extension(filename);
        MediaType contentType = switch (extension) {
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        return ResponseEntity.ok().contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                .body(new FileSystemResource(path));
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public record UploadResponse(String url, String originalFilename, long size) { }
}

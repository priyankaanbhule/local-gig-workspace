package com.localgig.auth;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Entity
@Table(name = "account_tokens")
public class AccountToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    // Only the SHA-256 digest is persisted. The raw token is available only
    // during creation so it can be delivered once to the user.
    @Column(nullable = false, unique = true, length = 64) private String token;
    @Transient private String rawToken;
    @Column(nullable = false, length = 20) private String type;
    @Column(nullable = false) private LocalDateTime expiresAt;
    protected AccountToken() { }
    public AccountToken(User user, String type, int expirationMinutes) {
        this.user = user;
        this.type = type;
        this.expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        this.rawToken = UUID.randomUUID().toString();
        this.token = hash(this.rawToken);
    }
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getToken() { return rawToken; }
    public String getType() { return type; }
    public LocalDateTime getExpiresAt() { return expiresAt; }

    public static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}

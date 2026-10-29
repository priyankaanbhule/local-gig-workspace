package com.localgig.notification;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 500) private String body;
    @Column(nullable = false, length = 30) private String type;
    @Column(nullable = false) private boolean read;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    protected Notification() { }
    public Notification(User recipient, String title, String body, String type) {
        this.recipient = recipient; this.title = title; this.body = body; this.type = type;
    }
    public void markRead() { read = true; }
    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

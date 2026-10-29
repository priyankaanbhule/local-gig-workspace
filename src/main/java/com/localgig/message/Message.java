package com.localgig.message;

import com.localgig.application.JobApplication;
import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    @Column(nullable = false, length = 3000) private String body;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false) private boolean read;
    protected Message() { }
    public Message(JobApplication application, User sender, String body) {
        this.application = application; this.sender = sender; this.body = body;
    }
    public void markRead() { read = true; }
    public Long getId() { return id; }
    public JobApplication getApplication() { return application; }
    public User getSender() { return sender; }
    public String getBody() { return body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
}

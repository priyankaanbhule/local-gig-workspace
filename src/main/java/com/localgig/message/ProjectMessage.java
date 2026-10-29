package com.localgig.message;

import com.localgig.project.Project;
import com.localgig.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_messages")
public class ProjectMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 3000)
    private String body;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean read;

    protected ProjectMessage() { }

    public ProjectMessage(Project project, User sender, String body) {
        this.project = project;
        this.sender = sender;
        this.body = body;
    }

    public void markRead() { read = true; }
    public Long getId() { return id; }
    public Project getProject() { return project; }
    public User getSender() { return sender; }
    public String getBody() { return body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return read; }
}

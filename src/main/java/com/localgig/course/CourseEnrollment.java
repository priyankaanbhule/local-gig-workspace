package com.localgig.course;

import com.localgig.user.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "course_enrollments", uniqueConstraints =
        @UniqueConstraint(name = "uk_course_enrollment_user", columnNames = {"course_id", "user_id"}))
public class CourseEnrollment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(nullable = false) private int completedLessons;
    private LocalDateTime completedAt;
    @Column(nullable = false) private LocalDateTime enrolledAt = LocalDateTime.now();
    protected CourseEnrollment() { }
    public CourseEnrollment(Course course, User user) { this.course = course; this.user = user; }
    public void setCompletedLessons(int count) {
        completedLessons = Math.max(0, Math.min(count, course.getLessonCount()));
        if (completedLessons == course.getLessonCount() && completedAt == null) completedAt = LocalDateTime.now();
        if (completedLessons < course.getLessonCount()) completedAt = null;
    }
    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public User getUser() { return user; }
    public int getCompletedLessons() { return completedLessons; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
}

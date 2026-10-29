package com.localgig.course;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 80) private String category;
    @Column(nullable = false, length = 2000) private String description;
    @Column(nullable = false) private int lessonCount;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "course_objectives", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "objective", length = 300)
    private List<String> objectives = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "course_lessons", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "lesson_title", nullable = false, length = 180)
    private List<String> lessons = new ArrayList<>();
    @Column(nullable = false) private boolean active = true;
    protected Course() { }
    public Course(String title, String category, String description, int lessonCount, List<String> objectives) {
        this(title, category, description, lessonCount, objectives, objectives);
    }
    public Course(String title, String category, String description, int lessonCount,
                  List<String> objectives, List<String> lessons) {
        this.title = title; this.category = category; this.description = description;
        this.lessonCount = lessonCount; this.objectives = new ArrayList<>(objectives);
        this.lessons = lessons == null ? new ArrayList<>() : new ArrayList<>(lessons);
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public int getLessonCount() { return lessonCount; }
    public List<String> getObjectives() { return objectives; }
    public List<String> getLessons() { return lessons; }
    public void initializeLessons(List<String> titles) {
        if (lessons == null || lessons.isEmpty()) lessons = new ArrayList<>(titles);
    }
}

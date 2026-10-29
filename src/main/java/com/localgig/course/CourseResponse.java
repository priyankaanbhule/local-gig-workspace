package com.localgig.course;

import java.time.LocalDateTime;
import java.util.List;

public record CourseResponse(Long id, String title, String category, String description,
                             int lessonCount, List<String> objectives, List<String> lessons,
                             Integer completedLessons, LocalDateTime completedAt) {
    public static CourseResponse catalog(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getCategory(),
                course.getDescription(), course.getLessonCount(), List.copyOf(course.getObjectives()),
                List.copyOf(course.getLessons()), null, null);
    }
    public static CourseResponse enrollment(CourseEnrollment enrollment) {
        Course c = enrollment.getCourse();
        return new CourseResponse(c.getId(), c.getTitle(), c.getCategory(), c.getDescription(),
                c.getLessonCount(), List.copyOf(c.getObjectives()), List.copyOf(c.getLessons()),
                enrollment.getCompletedLessons(), enrollment.getCompletedAt());
    }
}

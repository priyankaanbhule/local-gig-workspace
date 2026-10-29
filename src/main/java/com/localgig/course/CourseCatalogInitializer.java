package com.localgig.course;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class CourseCatalogInitializer {
    @Bean
    ApplicationRunner addStarterCourses(CourseRepository courses) {
        return args -> {
            if (courses.count() == 0) courses.saveAll(List.of(
                    new Course("Resume writing essentials", "Career readiness", "Build a clear, focused resume that highlights relevant experience.", 3,
                            List.of("Choose a clear resume structure", "Write skill-focused bullet points", "Tailor a resume to an opportunity"),
                            List.of("Choose a resume format", "Write strong experience bullets", "Tailor your resume to a role")),
                    new Course("Getting started with freelancing", "Freelance skills", "Learn how to scope projects, write proposals, and communicate with clients.", 4,
                            List.of("Define project scope", "Create a useful proposal", "Plan client communication"),
                            List.of("Choose a service", "Scope a client project", "Write a proposal", "Agree on communication and delivery")),
                    new Course("Interview confidence", "Career readiness", "Practice preparation and communication for common interview formats.", 3,
                            List.of("Research an employer", "Structure interview answers", "Prepare thoughtful questions"),
                            List.of("Research the employer", "Practice structured answers", "Prepare questions for the interviewer"))
            ));
            courses.findAllByActiveTrueOrderByCategoryAscTitleAsc().forEach(course -> {
                if (course.getLessons() == null || course.getLessons().isEmpty()) {
                    course.initializeLessons(course.getObjectives());
                }
            });
        };
    }
}

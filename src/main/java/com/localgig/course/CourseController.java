package com.localgig.course;

import com.localgig.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@Transactional
public class CourseController {
    private final CourseRepository courses;
    private final CourseEnrollmentRepository enrollments;
    public CourseController(CourseRepository courses, CourseEnrollmentRepository enrollments) {
        this.courses = courses; this.enrollments = enrollments;
    }
    @GetMapping
    public List<CourseResponse> catalog() {
        return courses.findAllByActiveTrueOrderByCategoryAscTitleAsc().stream().map(CourseResponse::catalog).toList();
    }
    @GetMapping("/me")
    public List<CourseResponse> myCourses(@AuthenticationPrincipal User user) {
        return enrollments.findAllByUserIdOrderByEnrolledAtDesc(user.getId()).stream().map(CourseResponse::enrollment).toList();
    }
    @PostMapping("/{courseId}/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse enroll(@AuthenticationPrincipal User user, @PathVariable Long courseId) {
        Course course = courses.findById(courseId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        CourseEnrollment enrollment = enrollments.findByCourseIdAndUserId(courseId, user.getId())
                .orElseGet(() -> enrollments.save(new CourseEnrollment(course, user)));
        return CourseResponse.enrollment(enrollment);
    }
    @PatchMapping("/{courseId}/progress")
    public CourseResponse progress(@AuthenticationPrincipal User user, @PathVariable Long courseId,
                                   @Valid @RequestBody CourseProgressRequest request) {
        CourseEnrollment enrollment = enrollments.findByCourseIdAndUserId(courseId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enroll in the course first"));
        if (request.completedLessons() > enrollment.getCourse().getLessonCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lesson count exceeds this course length");
        }
        enrollment.setCompletedLessons(request.completedLessons());
        return CourseResponse.enrollment(enrollment);
    }
    @GetMapping("/{courseId}/certificate")
    public CertificateResponse certificate(@AuthenticationPrincipal User user, @PathVariable Long courseId) {
        CourseEnrollment enrollment = enrollments.findByCourseIdAndUserId(courseId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course enrollment not found"));
        if (enrollment.getCompletedAt() == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Complete the course to earn its certificate");
        return new CertificateResponse("LG-COURSE-" + enrollment.getId(), enrollment.getCourse().getTitle(),
                user.getFullName(), enrollment.getCompletedAt());
    }
    public record CertificateResponse(String certificateId, String courseTitle, String learnerName,
                                      java.time.LocalDateTime completedAt) { }
}

package com.localgig.course;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {
    Optional<CourseEnrollment> findByCourseIdAndUserId(Long courseId, Long userId);
    List<CourseEnrollment> findAllByUserIdOrderByEnrolledAtDesc(Long userId);
}

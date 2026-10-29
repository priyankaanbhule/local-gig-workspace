package com.localgig.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByDeadlineReminderSentFalseAndStatus(ProjectStatus status);
    List<Project> findAllByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    List<Project> findAllByEmployerIdOrderByCreatedAtDesc(Long employerId);
    Optional<Project> findByIdAndFreelancerId(Long id, Long freelancerId);
    Optional<Project> findByIdAndEmployerId(Long id, Long employerId);
}

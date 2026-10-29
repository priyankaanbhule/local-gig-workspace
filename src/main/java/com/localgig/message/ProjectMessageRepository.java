package com.localgig.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMessageRepository extends JpaRepository<ProjectMessage, Long> {
    List<ProjectMessage> findAllByProjectIdOrderByCreatedAtAsc(Long projectId);
}

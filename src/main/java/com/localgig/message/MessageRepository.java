package com.localgig.message;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByApplicationIdOrderByCreatedAtAsc(Long applicationId);
}

package com.localgig.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    List<DirectMessage> findAllBySenderIdOrRecipientIdOrderByCreatedAtDesc(Long senderId, Long recipientId);
}

package com.localgig.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface AccountTokenRepository extends JpaRepository<AccountToken, Long> {
    Optional<AccountToken> findByTokenAndTypeAndExpiresAtAfter(String token, String type, LocalDateTime now);
    void deleteAllByUserIdAndType(Long userId, String type);
}

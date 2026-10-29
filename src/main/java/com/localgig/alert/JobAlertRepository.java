package com.localgig.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobAlertRepository extends JpaRepository<JobAlert, Long> {
    List<JobAlert> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    List<JobAlert> findAllByActiveTrue();
}

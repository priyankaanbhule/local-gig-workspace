package com.localgig.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ManagedCategoryRepository extends JpaRepository<ManagedCategory, Long> {
    List<ManagedCategory> findAllByScopeAndActiveTrueOrderByNameAsc(String scope);
    boolean existsByScopeAndNormalizedName(String scope, String normalizedName);
}

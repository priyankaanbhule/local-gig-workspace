package com.localgig.admin;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class ManagedCategoryInitializer {
    @Bean
    ApplicationRunner seedManagedCategories(ManagedCategoryRepository categories) {
        return args -> {
            if (categories.count() != 0) return;
            categories.saveAll(List.of(
                    new ManagedCategory("JOB", "Technology"), new ManagedCategory("JOB", "Design"),
                    new ManagedCategory("JOB", "Marketing"), new ManagedCategory("JOB", "Customer Support"),
                    new ManagedCategory("JOB", "Finance"), new ManagedCategory("JOB", "Operations"),
                    new ManagedCategory("COURSE", "Career readiness"), new ManagedCategory("COURSE", "Freelance skills"),
                    new ManagedCategory("COURSE", "Technology"), new ManagedCategory("COURSE", "Business")
            ));
        };
    }
}

package com.localgig.opportunity;

import com.localgig.common.validation.AllowedValues;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;
import java.util.List;

public record OpportunityRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 160) String company,
        @NotBlank @Size(max = 120) String location,
        @NotBlank @Size(max = 80) String category,
        @NotBlank @Size(max = 30) @AllowedValues(value = {"JOB", "INTERNSHIP", "GIG"}, message = "Choose Job, Internship, or Gig") String kind,
        @NotBlank @Size(max = 40) @AllowedValues(value = {"Full-time", "Part-time", "Remote", "Internship", "Freelance"}, message = "Choose a supported work type") String type,
        @NotBlank @Size(max = 120) String budget,
        @NotBlank @Size(max = 5000) String description,
        List<@Size(max = 80) String> skills,
        @FutureOrPresent LocalDate deadline,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
        @Min(0) @Max(50) Integer requiredExperienceYears
) { }

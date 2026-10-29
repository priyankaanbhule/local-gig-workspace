package com.localgig.course;

import jakarta.validation.constraints.PositiveOrZero;

public record CourseProgressRequest(@PositiveOrZero int completedLessons) { }

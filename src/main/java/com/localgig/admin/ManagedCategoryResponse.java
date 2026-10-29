package com.localgig.admin;

public record ManagedCategoryResponse(Long id, String scope, String name, boolean active) {
    public static ManagedCategoryResponse from(ManagedCategory category) {
        return new ManagedCategoryResponse(category.getId(), category.getScope(), category.getName(), category.isActive());
    }
}

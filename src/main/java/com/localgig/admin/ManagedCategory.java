package com.localgig.admin;

import jakarta.persistence.*;

@Entity
@Table(name = "managed_categories", uniqueConstraints =
        @UniqueConstraint(name = "uk_managed_category_scope_name", columnNames = {"scope", "normalized_name"}))
public class ManagedCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String scope;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(name = "normalized_name", nullable = false, length = 80)
    private String normalizedName;
    @Column(nullable = false)
    private boolean active = true;

    protected ManagedCategory() { }

    public ManagedCategory(String scope, String name) {
        this.scope = scope;
        updateName(name);
    }

    public void updateName(String name) {
        this.name = name.trim();
        this.normalizedName = this.name.toLowerCase(java.util.Locale.ROOT);
    }

    public Long getId() { return id; }
    public String getScope() { return scope; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
}

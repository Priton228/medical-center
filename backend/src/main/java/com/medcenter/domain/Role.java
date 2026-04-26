package com.medcenter.domain;

import com.medcenter.domain.enums.RoleName;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * Роль пользователя ({@link RoleName}). Связь many-to-many с {@link User} через {@code user_roles}.
 */
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 32)
    private RoleName name;

    public Role() {
    }

    public Role(RoleName name) {
        this.name = name;
    }

    public Role(Long id, RoleName name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RoleName getName() { return name; }
    public void setName(RoleName name) { this.name = name; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private RoleName name;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(RoleName name) { this.name = name; return this; }

        public Role build() {
            return new Role(id, name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name=" + name + "}";
    }
}

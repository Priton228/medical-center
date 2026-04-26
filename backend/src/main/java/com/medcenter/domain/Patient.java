package com.medcenter.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Профиль пациента. Связан 1:1 с {@link User} (через {@code user_id}).
 */
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(length = 255)
    private String address;

    @Column(name = "insurance_number", length = 64)
    private String insuranceNumber;

    public Patient() {
    }

    public Patient(Long id, User user, LocalDate birthDate, String address, String insuranceNumber) {
        this.id = id;
        this.user = user;
        this.birthDate = birthDate;
        this.address = address;
        this.insuranceNumber = insuranceNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private User user;
        private LocalDate birthDate;
        private String address;
        private String insuranceNumber;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder birthDate(LocalDate birthDate) { this.birthDate = birthDate; return this; }
        public Builder address(String address) { this.address = address; return this; }
        public Builder insuranceNumber(String insuranceNumber) { this.insuranceNumber = insuranceNumber; return this; }

        public Patient build() {
            return new Patient(id, user, birthDate, address, insuranceNumber);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Patient{id=" + id + "}";
    }
}

package com.medcenter.domain;

import jakarta.persistence.*;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Профиль врача. 1:1 связь с {@link User} (через {@code user_id}).
 */
@Entity
@Table(name = "doctors")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 128)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "work_start", nullable = false)
    private LocalTime workStart = LocalTime.of(9, 0);

    @Column(name = "work_end", nullable = false)
    private LocalTime workEnd = LocalTime.of(18, 0);

    @Column(name = "room_number", length = 16)
    private String roomNumber;

    public Doctor() {
    }

    public Doctor(Long id, User user, String specialization, String bio, String photoUrl,
                  boolean available, LocalTime workStart, LocalTime workEnd, String roomNumber) {
        this.id = id;
        this.user = user;
        this.specialization = specialization;
        this.bio = bio;
        this.photoUrl = photoUrl;
        this.available = available;
        this.workStart = workStart != null ? workStart : LocalTime.of(9, 0);
        this.workEnd = workEnd != null ? workEnd : LocalTime.of(18, 0);
        this.roomNumber = roomNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public LocalTime getWorkStart() { return workStart; }
    public void setWorkStart(LocalTime workStart) { this.workStart = workStart; }

    public LocalTime getWorkEnd() { return workEnd; }
    public void setWorkEnd(LocalTime workEnd) { this.workEnd = workEnd; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private User user;
        private String specialization;
        private String bio;
        private String photoUrl;
        private boolean available = true;
        private LocalTime workStart = LocalTime.of(9, 0);
        private LocalTime workEnd = LocalTime.of(18, 0);
        private String roomNumber;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder user(User user) { this.user = user; return this; }
        public Builder specialization(String specialization) { this.specialization = specialization; return this; }
        public Builder bio(String bio) { this.bio = bio; return this; }
        public Builder photoUrl(String photoUrl) { this.photoUrl = photoUrl; return this; }
        public Builder available(boolean available) { this.available = available; return this; }
        public Builder workStart(LocalTime workStart) { this.workStart = workStart; return this; }
        public Builder workEnd(LocalTime workEnd) { this.workEnd = workEnd; return this; }
        public Builder roomNumber(String roomNumber) { this.roomNumber = roomNumber; return this; }

        public Doctor build() {
            return new Doctor(id, user, specialization, bio, photoUrl, available, workStart, workEnd, roomNumber);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Doctor other)) return false;
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Doctor{id=" + id + ", specialization='" + specialization + "'}";
    }
}

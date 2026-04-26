package com.medcenter.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}

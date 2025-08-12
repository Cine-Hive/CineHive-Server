package com.example.CineHive.datasync.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "SyncTvCrew")
@Table(name = "tv_crew")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TvCrew {
    @Id
    @Column(name = "credit_id", length = 32)
    private String creditId;

    @Column(name = "tv_id", nullable = false)
    private Long tvId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(nullable = false)
    private String job;

    private String department;

    @Builder
    public TvCrew(String creditId, Long tvId, Long personId, String job, String department) {
        this.creditId = creditId;
        this.tvId = tvId;
        this.personId = personId;
        this.job = job;
        this.department = department;
    }
}
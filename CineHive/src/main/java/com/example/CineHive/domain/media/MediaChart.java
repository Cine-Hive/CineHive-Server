package com.example.CineHive.domain.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"media_id", "chart_type"}))
public class MediaChart {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChartType chartType; // POPULAR_MOVIE, UPCOMING_MOVIE, POPULAR_TV, etc.

    @Column(nullable = false)
    private Integer rank;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        updatedAt = LocalDateTime.now();
    }
}

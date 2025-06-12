package com.example.CineHive.entity.media;

import com.example.CineHive.entity.credit.Director;
import jakarta.persistence.*;
import lombok.*;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "media_type")
public abstract class Media {

    @Id
    private Long id;

    private String title;
    private String originalTitle;
    private String overview;
    private String releaseDate;
    private Double voteAverage;
    private Integer voteCount;
    private Double popularity;
    private String posterPath;
    private String backdropPath;

    @Column(nullable = false)
    private boolean isAnimation = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MediaGenre> mediaGenres;

    @OneToMany(mappedBy = "media", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Director> director;
}

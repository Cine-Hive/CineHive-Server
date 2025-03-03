package com.example.CineHive.entity.videotype;

import com.example.CineHive.entity.credit.animation.Director;
import com.example.CineHive.entity.credit.animation.Genre;
import com.example.CineHive.entity.credit.animation.Video;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "animation")
public class Animation {

    @Id
    private Long id;
    private String name;

    @Lob
    private String overview;
    private String posterPath;
    private String backDropPath;
    @Column(name = "release_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    private double voteAverage;
    private double popularity;


    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "animation_id")
    private List<Genre> genres = new ArrayList<>();

    @OneToMany(mappedBy = "animation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Director> directors = new ArrayList<>();


    @OneToMany(mappedBy = "animation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Video> videos = new ArrayList<>();
}

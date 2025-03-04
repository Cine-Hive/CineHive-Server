package com.example.CineHive.entity.credit.movie;

import com.example.CineHive.entity.videotype.TopMovie;
import com.example.CineHive.entity.videotype.UpComingMovie;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpComingGenre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    @JsonIgnore
    private UpComingMovie movie;

}

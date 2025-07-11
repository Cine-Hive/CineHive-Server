package com.example.CineHive.entity.media;


import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("TV")
public class TvSeries extends Media {
    private Integer numberOfSeasons;
    private Integer numberOfEpisodes;
}

package com.example.CineHive.entity.credit;

import jakarta.persistence.*;
import com.example.CineHive.entity.media.Media;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Director {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Person person;
}

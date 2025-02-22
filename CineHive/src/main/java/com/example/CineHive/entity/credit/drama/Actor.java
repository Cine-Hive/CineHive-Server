package com.example.CineHive.entity.credit.drama;

import com.example.CineHive.entity.videotype.Drama;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DramaActor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "drama_actors")
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drama_id", nullable = false)
    @JsonIgnore
    private Drama drama;

}

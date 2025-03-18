package com.example.CineHive.entity.credit.animation;

import com.example.CineHive.entity.videotype.Animation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "AnimationGenre") // 엔티티 이름을 명시적으로 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="animation_genres")
public class Genre {
    @Id
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "animation_id")
    @JsonIgnore
    private Animation animation;
}

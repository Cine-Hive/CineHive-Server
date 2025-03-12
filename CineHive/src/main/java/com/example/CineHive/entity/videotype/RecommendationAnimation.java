package com.example.CineHive.entity.videotype;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationAnimation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "animation_id")
    private Animation animation;

    @ManyToOne
    @JoinColumn(name = "recommended_animation_id")
    private Animation recommendedAnimation;
}

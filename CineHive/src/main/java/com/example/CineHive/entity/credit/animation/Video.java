package com.example.CineHive.entity.credit.animation;

import com.example.CineHive.entity.videotype.Animation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "animation_videos")
@Entity(name="AnimationVideo")
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoKey; // 유튜브 비디오 키
    private String name;

    @ManyToOne
    @JoinColumn(name = "animation_id")
    @JsonIgnore
    private Animation animation;

}

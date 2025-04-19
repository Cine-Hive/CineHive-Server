package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    @Id
    private Integer id;
    
    private String name;
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    
    public enum MediaType {
        MOVIE, TV, ANIMATION, COMMON
    }
} 
package com.example.CineHive.entity.credit;

import com.example.CineHive.entity.media.Media;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Crew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long personId;
    private String name;
    private String job;
    private String department;
    private String profilePath;
    
    @Column(name = "media_id")
    private Long mediaId;
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private Media.MediaType mediaType;
} 
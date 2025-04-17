package com.example.CineHive.entity.credit;

import com.example.CineHive.entity.media.Media;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "casts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long castId;
    private Long personId;
    private String name;
    private String character;
    private String profilePath;
    private Integer order;
    
    @Column(name = "media_id")
    private Long mediaId;
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private Media.MediaType mediaType;
} 
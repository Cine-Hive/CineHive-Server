package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "media_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MediaGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "media_id")
    private Long mediaId;
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private Media.MediaType mediaType;
    
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
} 
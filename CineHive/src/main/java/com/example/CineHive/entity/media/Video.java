package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "CommonVideo")
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    @Id
    private String id;
    
    private String name;
    
    @Column(name = "video_key")
    private String videoKey;
    
    private String site;
    private String type;
    
    @Column(name = "media_id")
    private Long mediaId;
    
    @Column(name = "media_type")
    @Enumerated(EnumType.STRING)
    private Media.MediaType mediaType;
} 
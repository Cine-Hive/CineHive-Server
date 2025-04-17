package com.example.CineHive.entity.media;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity(name = "CommonVideo")
@Table(name = "videos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"movie_id", "video_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    @Id
    @Schema(description = "비디오 ID", example = "5c9294240e0a267cd516835f")
    private String id;
    
    @Schema(description = "비디오 제목", example = "파이트 클럽 공식 예고편")
    @Column(nullable = false)
    private String name;
    
    @Schema(description = "비디오 키", example = "BdJKm16Co6M")
    @Column(name = "video_key", nullable = false)
    private String videoKey;
    
    @Schema(description = "비디오 사이트", example = "YouTube")
    @Column(nullable = false, length = 50)
    private String site;
    
    @Schema(description = "비디오 유형", example = "Trailer")
    @Column(nullable = false, length = 50)
    private String type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    @Schema(description = "연관된 영화")
    private Movie movie;
} 
package com.example.CineHive.entity.credit;

import com.example.CineHive.entity.media.Media;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "crews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Crew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "제작진 ID", example = "1")
    private Long id;
    
    @Schema(description = "인물 ID", example = "7467")
    private Long personId;
    
    @Schema(description = "제작진 이름", example = "데이비드 핀처")
    @Column(nullable = false)
    private String name;
    
    @Schema(description = "직무", example = "Director")
    @Column(nullable = false)
    private String job;
    
    @Schema(description = "부서", example = "Directing")
    @Column(nullable = false)
    private String department;
    
    @Schema(description = "프로필 이미지 경로", example = "/path/to/profile.jpg")
    private String profilePath;
    
    @Schema(description = "미디어 ID", example = "550")
    @Column(nullable = false)
    private Long mediaId;
    
    @Schema(description = "미디어 타입", example = "MOVIE")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Media.MediaType mediaType;
}
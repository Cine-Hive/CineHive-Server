package com.example.CineHive.entity.credit;

import com.example.CineHive.entity.media.Media;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "casts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cast {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "출연진 ID", example = "1")
    private Long id;
    
    @Schema(description = "출연진 캐스팅 ID", example = "52fe4250c3a36847f8014a11")
    private Long castId;
    
    @Schema(description = "인물 ID", example = "819")
    private Long personId;
    
    @Schema(description = "배우 이름", example = "브래드 피트")
    @Column(nullable = false)
    private String name;

    @Schema(description = "배역", example = "타일러 더든")
    @Column(name = "`character`", nullable = false, columnDefinition = "TEXT")
    private String character;
    
    @Schema(description = "프로필 이미지 경로", example = "/path/to/profile.jpg")
    private String profilePath;

    @Schema(description = "출연 순서", example = "1")
    @Column(name = "`order`")
    private Integer order;
    
    @Schema(description = "미디어 ID", example = "550")
    @Column(nullable = false)
    private Long mediaId;
    
    @Schema(description = "미디어 타입", example = "MOVIE")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Media.MediaType mediaType;
}
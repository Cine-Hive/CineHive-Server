package com.example.CineHive.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "제작진 정보")
public class CrewDto {
    @Schema(description = "인물 ID", example = "7467")
    private Long id;
    
    @Schema(description = "제작진 이름", example = "데이비드 핀처")
    private String name;
    
    @Schema(description = "직무", example = "Director")
    private String job;
    
    @Schema(description = "부서", example = "Directing")
    private String department;
    
    @Schema(description = "프로필 이미지 경로", example = "/path/to/profile.jpg")
    private String profilePath;
} 
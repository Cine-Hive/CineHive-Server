package com.example.CineHive.controller;

import com.example.CineHive.dto.video.common.VideoDto;
import com.example.CineHive.entity.videotype.Drama;
import com.example.CineHive.repository.videos.drama.DramaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Tag(name = "Drama Controller", description = "드라마 정보 관련 기능을 제공하는 API")
@Controller
public class DramaController {

    @Autowired
    private DramaRepository dramaRepository;

    @Operation(summary = "Drama 상세 페이지 받아오기", description = "해당 Drama ID로 영화 상세 정보를 상세 페이지에 반환, 존재하지 않는 경우 404 응답을 반환")
    @GetMapping("/dramas/{id}")
    @ResponseBody
    public ResponseEntity<VideoDto> getDramaById(@PathVariable Long id) {
        Optional<Drama> dramaOptional = dramaRepository.findById(id);
        if (dramaOptional.isPresent()) {
            return ResponseEntity.ok(convertToVideoDto(dramaOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Drama 목록 받아오기", description = "drama 테이블에 저장된 모든 drama 정보를 리스트 형태로 반환")
    @GetMapping("/dramas")
    @ResponseBody
    public List<VideoDto> getDramas(){
        return dramaRepository.findAll().stream()
                .map(this::convertToVideoDto)
                .collect(Collectors.toList());
    }
    
    // Drama 객체를 VideoDto로 변환하는 헬퍼 메서드
    private VideoDto convertToVideoDto(Drama drama) {
        List<String> genreNames = drama.getGenres().stream()
                .map(genre -> genre.getName())
                .collect(Collectors.toList());
                
        return new VideoDto(
                drama.getId(),
                drama.getPosterPath(),
                drama.getName(),
                drama.getFirstAirDate(),
                genreNames
        );
    }
}

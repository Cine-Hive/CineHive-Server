package com.example.CineHive.controller;

import com.example.CineHive.entity.videotype.Animation;
import com.example.CineHive.repository.videos.animation.AnimationRepository;
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

@Tag(name = "Animation Controller", description = "애니메이션 정보 관련 기능을 제공하는 API")
@Controller
public class  AnimationController {

    @Autowired
    private AnimationRepository animationRepository;
    @Operation(summary = "Animation 상세 페이지 받아오기", description = "해당 Animation ID로 Animation 정보를 상세 페이지에 반환, 존재하지 않는 경우 404 응답을 반환")
    @GetMapping("/animations/{id}")
    @ResponseBody
    public ResponseEntity<Animation> getAnimationById(@PathVariable Long id) {
        Optional<Animation> animationOptional = animationRepository.findById(id);
        if (animationOptional.isPresent()) {
            return ResponseEntity.ok(animationOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Animation 목록 받아오기", description = "animation 테이블에 저장된 모든 animation 정보를 리스트 형태로 반환")
    @GetMapping("/animations")
    @ResponseBody
    public List<Animation> getMovies(){
        return animationRepository.findAll();
    }
}

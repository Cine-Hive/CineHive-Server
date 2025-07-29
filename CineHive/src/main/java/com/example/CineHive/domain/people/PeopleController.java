package com.example.CineHive.domain.people;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인물(배우, 감독 등) 정보 조회 및 관련 상호작용 API 컨트롤러입니다.
 */
@Tag(name = "People Controller", description = "인물 정보 조회 및 상호작용 API")
@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PeopleController {

    // TODO: private final PeopleService peopleService;

    @Operation(summary = "인기 인물 목록 조회",
            description = "현재 인기 있는 인물(배우, 감독 등) 목록을 조회합니다.")
    @GetMapping("/trends/popular")
    public void getPopularPeople() {
        // TODO: 1. PeopleService에서 인기 인물 목록 조회 로직 호출 (페이징 처리 고려)
        // TODO: 2. 조회된 데이터를 PeopleSummaryResponse DTO 리스트로 변환하여 반환
    }

    @Operation(summary = "특정 인물 상세 정보 조회",
            description = "특정 인물의 상세 프로필, 최신 활동 등의 정보를 조회합니다.")
    @GetMapping("/{personId}")
    public void getPersonDetails(@PathVariable Long personId) {
        // TODO: 1. PeopleService에서 personId로 인물 상세 정보 조회
        // TODO: 2. 조회된 데이터를 PersonDetailResponse DTO로 변환하여 반환
    }

    @Operation(summary = "인물 필모그래피 조회",
            description = "특정 인물이 참여한 작품 목록(필모그래피)을 조회합니다.")
    @GetMapping("/{personId}/filmography")
    public void getPersonFilmography(@PathVariable Long personId) {
        // TODO: 1. PeopleService에서 personId의 필모그래피 조회
        // TODO: 2. 조회된 데이터를 FilmographyResponse DTO 리스트로 변환하여 반환
    }

    @Operation(summary = "인물 '좋아요' 하기",
            description = "특정 인물에 대해 '좋아요'를 표시합니다.")
    @PostMapping("/{personId}/like")
    public void likePerson(
            @PathVariable Long personId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. 현재 로그인한 사용자 정보와 personId를 사용하여 '좋아요' 처리
        // TODO: 2. PeopleService/LikeService의 likePerson(userEmail, personId) 호출
        // TODO: 3. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "인물 '좋아요' 취소",
            description = "특정 인물에 대해 눌렀던 '좋아요'를 취소합니다.")
    @DeleteMapping("/{personId}/like")
    public void unlikePerson(
            @PathVariable Long personId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. 현재 로그인한 사용자 정보와 personId를 사용하여 '좋아요' 취소 처리
        // TODO: 2. PeopleService/LikeService의 unlikePerson(userEmail, personId) 호출
        // TODO: 3. 성공 시 MessageResponse 반환
    }
}
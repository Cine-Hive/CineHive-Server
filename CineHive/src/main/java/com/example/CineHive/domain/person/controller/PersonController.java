package com.example.CineHive.domain.person.controller;

import com.example.CineHive.domain.person.dto.FilmographyResponse;
import com.example.CineHive.domain.person.dto.PersonDetailsResponse;
import com.example.CineHive.domain.person.dto.PersonInListResponse;
import com.example.CineHive.domain.person.service.PersonLikeService;
import com.example.CineHive.domain.person.service.PersonQueryService;
import com.example.CineHive.global.dto.MessageResponse;
import com.example.CineHive.global.dto.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Person Controller", description = "인물(배우/감독) 정보 조회 및 상호작용 API")
@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonQueryService personQueryService;
    private final PersonLikeService personLikeService;

    @Operation(summary = "특정 인물 상세 정보 조회", description = "TMDB ID를 사용하여 특정 인물의 상세 프로필 정보를 조회합니다.")
    @GetMapping("/{personTmdbId}")
    public PersonDetailsResponse getPersonDetails(@PathVariable Long personTmdbId) {
        return personQueryService.getPersonDetails(personTmdbId);
    }

    @Operation(summary = "인물 '좋아요' 등록", description = "로그인한 사용자가 특정 인물에 대해 '좋아요'를 표시합니다.")
    @PostMapping("/{personTmdbId}/likes")
    public MessageResponse likePerson(
            @PathVariable Long personTmdbId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        personLikeService.like(userDetails.getUsername(), personTmdbId);
        return new MessageResponse("정상적으로 '좋아요' 처리되었습니다.");
    }

    @Operation(summary = "인물 '좋아요' 취소", description = "로그인한 사용자가 특정 인물에 대해 눌렀던 '좋아요'를 취소합니다.")
    @DeleteMapping("/{personTmdbId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePerson(
            @PathVariable Long personTmdbId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        personLikeService.unlike(userDetails.getUsername(), personTmdbId);
    }

    @Operation(summary = "인기 인물 목록 조회", description = "현재 인기 있는 인물 목록을 슬라이스하여 조회합니다.")
    @Parameters({
            @Parameter(name = "page", description = "조회할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "한 페이지에 표시할 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10")),
            @Parameter(name = "sort", hidden = true)
    })
    @GetMapping("/trends/popular")
    public SliceResponse<PersonInListResponse> getPopularPeople(
            @Parameter(hidden = true) @ParameterObject Pageable pageable) {
        return personQueryService.getPopularPeople(pageable);
    }

    @Operation(summary = "인물 필모그래피 조회",
            description = """
                    ### **특정 인물이 참여한 작품 목록(필모그래피)을 슬라이스하여 조회합니다.**
                    
                    **[정렬]**
                    - 작품의 개봉/방영일 최신순으로 정렬됩니다.
                    
                    **[페이징 파라미터]**
                    - `page`: 조회할 페이지 번호 (**0부터 시작**)
                    - `size`: 한 페이지에 표시할 작품 수
                    """)
    @Parameters({
            @Parameter(name = "page", description = "조회할 페이지 번호 (0부터 시작)", example = "0", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "0")),
            @Parameter(name = "size", description = "한 페이지에 표시할 항목 수", example = "10", in = ParameterIn.QUERY, schema = @Schema(type = "integer", defaultValue = "10"))
    })
    @GetMapping("/{personTmdbId}/filmography")
    public SliceResponse<FilmographyResponse> getPersonFilmography(
            @PathVariable Long personTmdbId,
            @Parameter(hidden = true) @ParameterObject Pageable pageable) {
        return personQueryService.getFilmography(personTmdbId, pageable);
    }
}
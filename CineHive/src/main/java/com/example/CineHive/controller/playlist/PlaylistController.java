package com.example.CineHive.controller.playlist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 플레이리스트의 CRUD 및 내부 미디어 관리를 담당하는 API 컨트롤러입니다.
 */
@Tag(name = "Playlist Controller", description = "사용자 플레이리스트 관리 API")
@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    // TODO: private final PlaylistService playlistService;

    @Operation(summary = "새 플레이리스트 생성")
    @PostMapping
    public void createPlaylist(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. CreatePlaylistRequest DTO를 @RequestBody로 받음
        // TODO: 2. PlaylistService.createPlaylist(userEmail, request) 호출
        // TODO: 3. 성공(201 CREATED) 시 생성된 PlaylistDetailResponse 반환
    }

    @Operation(summary = "플레이리스트 상세 조회")
    @GetMapping("/{playlistId}")
    public void getPlaylistDetails(@PathVariable Long playlistId) {
        // TODO: 1. PlaylistService.getPlaylistDetails(playlistId) 호출
        // TODO: 2. (중요) 플레이리스트가 공개(public) 상태이거나, 현재 사용자가 소유자인 경우에만 조회 허용
        // TODO: 3. PlaylistDetailResponse (신규 DTO)로 변환하여 반환
    }

    @Operation(summary = "플레이리스트 수정",
            description = "플레이리스트의 제목, 설명 등 메타데이터를 수정합니다.")
    @PatchMapping("/{playlistId}")
    public void updatePlaylist(
            @PathVariable Long playlistId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. UpdatePlaylistRequest DTO를 @RequestBody로 받음
        // TODO: 2. PlaylistService.updatePlaylist(userEmail, playlistId, request) 호출 (소유권 검증 포함)
        // TODO: 3. 성공 시 수정된 PlaylistDetailResponse 반환
    }

    @Operation(summary = "플레이리스트 삭제")
    @DeleteMapping("/{playlistId}")
    public void deletePlaylist(
            @PathVariable Long playlistId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. PlaylistService.deletePlaylist(userEmail, playlistId) 호출 (소유권 검증 포함)
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "플레이리스트에 미디어 추가")
    @PostMapping("/{playlistId}/media")
    public void addMediaToPlaylist(
            @PathVariable Long playlistId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. AddMediaRequest DTO (mediaType, mediaId 포함)를 @RequestBody로 받음
        // TODO: 2. PlaylistService.addMediaToPlaylist(userEmail, playlistId, request) 호출 (소유권 검증 포함)
        // TODO: 3. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "플레이리스트에서 미디어 삭제")
    @DeleteMapping("/{playlistId}/media/{tmdbMediaId}")
    public void removeMediaFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long tmdbMediaId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. PlaylistService.removeMediaFromPlaylist(userEmail, playlistId, tmdbMediaId) 호출 (소유권 검증 포함)
        // TODO: 2. 성공 시 MessageResponse 반환
    }
}
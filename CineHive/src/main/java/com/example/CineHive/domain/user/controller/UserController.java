package com.example.CineHive.domain.user.controller.entity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 타 사용자 프로필 조회, 팔로우, 활동 내역 등 사용자 간 상호작용 API 컨트롤러입니다.
 */
@Tag(name = "User Controller", description = "타 사용자 조회 및 상호작용 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    // TODO: private final UserService userService;
    // TODO: private final FollowService followService;
    // TODO: private final PostService postService;
    // TODO: private final PlaylistService playlistService;

    @Operation(summary = "타 사용자 공개 프로필 상세정보 조회")
    @GetMapping("/{userId}/profile")
    public void getUserProfile(@PathVariable Long userId) {
        // TODO: 1. UserService에서 userId로 사용자 프로필 정보 조회
        // TODO: 2. 조회된 정보를 UserProfileResponse DTO로 변환하여 반환
    }

    @Operation(summary = "특정 사용자의 팔로워 목록 조회")
    @GetMapping("/{userId}/followers")
    public void getFollowers(@PathVariable Long userId) {
        // TODO: 1. FollowService에서 userId의 팔로워 목록 조회 (페이징 처리)
        // TODO: 2. PagedResponse<UserProfileResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "특정 사용자의 팔로잉 목록 조회")
    @GetMapping("/{userId}/followings")
    public void getFollowings(@PathVariable Long userId) {
        // TODO: 1. FollowService에서 userId가 팔로잉하는 목록 조회 (페이징 처리)
        // TODO: 2. PagedResponse<UserProfileResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "다른 사용자 팔로우")
    @PostMapping("/{userId}/follow")
    public void followUser(
            @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. FollowService.follow(followerEmail, followingId) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "다른 사용자 언팔로우")
    @DeleteMapping("/{userId}/follow")
    public void unfollowUser(
            @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. FollowService.unfollow(followerEmail, followingId) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "특정 사용자가 작성한 게시글 목록 조회")
    @GetMapping("/{userId}/posts")
    public void getUserPosts(@PathVariable Long userId) {
        // TODO: 1. PostService에서 userId로 작성된 게시글 목록 조회 (페이징 처리)
        // TODO: 2. PagedResponse<PostSummaryResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "특정 사용자가 생성한 플레이리스트 목록 조회")
    @GetMapping("/{userId}/playlists")
    public void getUserPlaylists(@PathVariable Long userId) {
        // TODO: 1. PlaylistService에서 userId로 생성된 플레이리스트 목록 조회 (페이징 처리)
        // TODO: 2. PlaylistSummaryResponse (신규 DTO) 리스트로 변환하여 반환
    }
}
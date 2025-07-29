package com.example.CineHive.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증된 사용자 본인(me)의 계정 정보 및 활동 내역 조회 API 컨트롤러입니다.
 */
@Tag(name = "Account Controller", description = "내 계정 정보 및 활동 관리 API")
@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class AccountController {

    // TODO: private final AccountService accountService;

    // =========================================
    // == 계정 관리
    // =========================================

    @Operation(summary = "내 정보 상세 조회")
    @GetMapping
    public void getMyInfo(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. AccountService.getAccountInfo(userEmail) 호출
        // TODO: 2. AccountInfoResponse DTO로 변환하여 반환
    }

    @Operation(summary = "내 정보 수정",
            description = "닉네임, 프로필 이미지, 최근 검색어 저장 여부 등 내 계정 정보를 수정합니다.")
    @PatchMapping
    public void updateMyInfo(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. AccountUpdateRequest DTO를 @RequestBody로 받음
        // TODO: 2. AccountService.updateAccountInfo(userEmail, request) 호출
        // TODO: 3. 수정된 정보가 포함된 AccountInfoResponse DTO 반환
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public void deleteMyAccount(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. AccountService.deleteAccount(userEmail) 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    // =========================================
    // == 내 활동 조회
    // =========================================

    @Operation(summary = "내 북마크 목록 조회")
    @GetMapping("/bookmarks")
    public void getMyBookmarks(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. BookmarkService에서 현재 사용자의 북마크 목록 조회 (페이징)
        // TODO: 2. PagedResponse<PostSummaryResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "내 플레이리스트 목록 조회")
    @GetMapping("/playlists")
    public void getMyPlaylists(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. PlaylistService에서 현재 사용자의 플레이리스트 목록 조회 (페이징)
        // TODO: 2. PagedResponse<PlaylistSummaryResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "내가 좋아요한 인물 목록")
    @GetMapping("/liked-people")
    public void getMyLikedPeople(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. '좋아요한 인물' 서비스에서 목록 조회 (페이징)
        // TODO: 2. PagedResponse<PersonSummaryResponse> 형태로 변환하여 반환
    }

    @Operation(summary = "내 커뮤니티 활동 내역")
    @GetMapping("/activities")
    public void getMyActivities(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. 내가 작성한 게시글/댓글 등을 시간순으로 조회 (페이징)
        // TODO: 2. PagedResponse<ActivityResponse> (신규 DTO) 형태로 변환하여 반환
    }

    @Operation(summary = "내 시청 상태별 미디어 목록")
    @GetMapping("/media-list")
    public void getMyMediaListByStatus(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. '시청 상태'를 @RequestParam으로 받아 필터링
        // TODO: 2. PagedResponse<MyMediaStatusResponse> (신규 DTO) 형태로 변환하여 반환
    }

    @Operation(summary = "내 최근 검색어 목록 조회")
    @GetMapping("/recent-searches")
    public void getMyRecentSearches(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. SearchService/AccountService에서 최근 검색어 목록 조회
        // TODO: 2. RecentSearchResponse (신규 DTO) 리스트로 변환하여 반환
    }

    @Operation(summary = "내 최근 검색어 전체 삭제")
    @DeleteMapping("/recent-searches")
    public void deleteAllMyRecentSearches(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. SearchService/AccountService에서 최근 검색어 전체 삭제 로직 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "특정 검색어 기록 삭제")
    @DeleteMapping("/recent-searches/{searchId}")
    public void deleteMyRecentSearch(
            @PathVariable Long searchId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. SearchService/AccountService에서 특정 검색어 삭제 로직 호출
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    // =========================================
    // == 내 설정 관리
    // =========================================

    @Operation(summary = "내 알림 설정 조회")
    @GetMapping("/notification-settings")
    public void getMyNotificationSettings(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService에서 현재 사용자의 알림 설정 조회
        // TODO: 2. NotificationSettingsResponse (신규 DTO)로 변환하여 반환
    }

    @Operation(summary = "내 알림 설정 변경")
    @PutMapping("/notification-settings")
    public void updateMyNotificationSettings(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. UpdateNotificationSettingsRequest (신규 DTO)를 @RequestBody로 받음
        // TODO: 2. NotificationService 호출하여 알림 설정 업데이트
        // TODO: 3. 성공 시 MessageResponse 반환
    }
}
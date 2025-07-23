package com.example.CineHive.controller.user;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Controller", description = "타 사용자 프로필 조회 및 팔로우 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    // TODO: UserService 또는 FollowService 의존성 주입 필요
    // private final UserService userService;

    /**
     * 특정 사용자의 공개 프로필 정보를 조회합니다.
     */
    @GetMapping("/{userId}/profile")
    public void getUserProfile(@PathVariable Long userId) {
        // TODO: 1. UserService에서 userId로 사용자 프로필 정보 조회
        // TODO: 2. 조회된 정보를 UserProfileResponse DTO로 변환
        // TODO: 3. ApiResponse.ok()로 감싸서 ResponseEntity<ApiResponse<UserProfileResponse>> 반환
    }

    /**
     * 특정 사용자를 팔로우하는 사용자(팔로워) 목록을 조회합니다.
     */
    @GetMapping("/{userId}/followers")
    public void getFollowers(@PathVariable Long userId) {
        // TODO: 1. UserService/FollowService에서 userId의 팔로워 목록 조회 (페이징 처리 고려)
        // TODO: 2. 조회된 사용자 목록을 UserProfileResponse DTO 리스트로 변환
        // TODO: 3. ApiResponse.ok()로 감싸서 PagedResponse 또는 List 형태로 반환
    }

    /**
     * 특정 사용자가 팔로우하는 사용자(팔로잉) 목록을 조회합니다.
     */
    @GetMapping("/{userId}/followings")
    public void getFollowings(@PathVariable Long userId) {
        // TODO: 1. UserService/FollowService에서 userId가 팔로잉하는 목록 조회 (페이징 처리 고려)
        // TODO: 2. 조회된 사용자 목록을 UserProfileResponse DTO 리스트로 변환
        // TODO: 3. ApiResponse.ok()로 감싸서 PagedResponse 또는 List 형태로 반환
    }

    /**
     * 특정 사용자를 팔로우합니다.
     */
    @PostMapping("/{userId}/follow")
    public void followUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. 현재 로그인한 사용자(follower)의 이메일을 userDetails.getUsername()으로 가져오기
        // TODO: 2. 팔로우 대상(following)의 ID는 @PathVariable로 받은 userId
        // TODO: 3. 본인을 팔로우하려는 경우 예외 처리
        // TODO: 4. 이미 팔로우한 경우 예외 처리
        // TODO: 5. UserService/FollowService.follow(followerEmail, followingId) 호출
        // TODO: 6. 성공 시 MessageResponse로 응답 (예: "{nickname}님을 팔로우했습니다.")
    }

    /**
     * 특정 사용자를 언팔로우합니다.
     */
    @DeleteMapping("/{userId}/follow")
    public void unfollowUser(@PathVariable Long userId, @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. 현재 로그인한 사용자(follower)의 이메일을 userDetails.getUsername()으로 가져오기
        // TODO: 2. 언팔로우 대상(following)의 ID는 @PathVariable로 받은 userId
        // TODO: 3. 팔로우 관계가 아닌 경우 예외 처리
        // TODO: 4. UserService/FollowService.unfollow(followerEmail, followingId) 호출
        // TODO: 5. 성공 시 MessageResponse로 응답 (예: "{nickname}님을 언팔로우했습니다.")
    }
}
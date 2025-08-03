package com.example.CineHive.domain.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자용 사용자 관리 API 컨트롤러입니다.
 */
@Tag(name = "Admin User Controller", description = "관리자용 사용자 관리 API")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    @Operation(summary = "사용자 목록 조회")
    @GetMapping
    public void getUserList() {
        // TODO: 1. 검색/필터링 조건을 @RequestParam으로 받음 (페이징 포함)
        // TODO: 2. AdminUserService.getUsers(searchCondition, pageable) 호출
        // TODO: 3. PagedResponse<UserAdminSummaryResponse> DTO로 변환하여 반환
    }

    @Operation(summary = "특정 사용자 상세 정보 조회")
    @GetMapping("/{userId}")
    public void getUserDetails(@PathVariable Long userId) {
        // TODO: 1. AdminUserService.getUserDetails(userId) 호출
        // TODO: 2. UserAdminDetailResponse DTO로 변환하여 반환
    }

    @Operation(summary = "사용자 정보/상태 수정")
    @PatchMapping("/{userId}")
    public void updateUserStatus(@PathVariable Long userId) {
        // TODO: 1. UpdateUserStatusRequest DTO를 @RequestBody로 받음
        // TODO: 2. AdminUserService.updateUser(userId, request) 호출
        // TODO: 3. 수정된 UserAdminDetailResponse DTO 반환
    }

    @Operation(summary = "특정 사용자 활동 내역 조회")
    @GetMapping("/{userId}/activities")
    public void getUserActivities(@PathVariable Long userId) {
        // TODO: 1. AdminUserService.getUserActivities(userId, pageable) 호출 (페이징)
        // TODO: 2. PagedResponse<ActivityResponse> DTO로 변환하여 반환
    }
}
<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/notification/controller/NotificationController.java
package com.example.CineHive.domain.notification.controller;
=======
package com.example.CineHive.domain.notification;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/notification/NotificationController.java

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 사용자 알림 관련 API 컨트롤러입니다.
 */
@Tag(name = "Notification Controller", description = "사용자 알림 API")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    // TODO: private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회")
    @GetMapping("/api/v1/users/me/notifications")
    public void getMyNotifications(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService에서 현재 사용자의 알림 목록 조회 (페이징)
        // TODO: 2. PagedResponse<NotificationResponse> (신규 DTO) 형태로 반환
    }

    @Operation(summary = "읽지 않은 알림 상태 확인",
            description = "읽지 않은 알림이 있는지 여부와 개수를 확인합니다.")
    @GetMapping("/api/v1/users/me/notifications/status")
    public void getUnreadNotificationStatus(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService에서 읽지 않은 알림 상태 조회
        // TODO: 2. UnreadStatusResponse (신규 DTO) 형태로 반환 (e.g., {"hasUnread": true, "count": 5})
    }

    @Operation(summary = "모든 알림 읽음 처리")
    @PostMapping("/api/v1/users/me/notifications/read-all")
    public void readAllMyNotifications(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService에서 모든 알림을 읽음 상태로 변경
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "특정 알림 읽음 처리")
    @PatchMapping("/api/v1/users/me/notifications/{notificationId}")
    public void readNotification(
            @PathVariable Long notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService에서 특정 알림을 읽음 상태로 변경 (소유권 검증 포함)
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "특정 알림 삭제")
    @DeleteMapping("/api/v1/users/me/notifications/{notificationId}")
    public void deleteNotification(
            @PathVariable Long notificationId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService에서 특정 알림 삭제 (소유권 검증 포함)
        // TODO: 2. 성공 시 MessageResponse 반환
    }

    @Operation(summary = "실시간 알림 구독 (SSE)",
            description = "Server-Sent Events(SSE)를 통해 실시간으로 알림을 수신하는 연결을 설정합니다.")
    @GetMapping(value = "/api/v1/notifications/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 1. NotificationService.subscribe(userEmail) 호출
        // TODO: 2. 반환된 SseEmitter를 클라이언트에 전달
        return null; // 실제 구현에서는 service 계층에서 생성된 SseEmitter 객체를 반환
    }
}
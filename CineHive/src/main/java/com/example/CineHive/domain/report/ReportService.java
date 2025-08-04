<<<<<<< HEAD:CineHive/src/main/java/com/example/CineHive/domain/report/service/ReportService.java
package com.example.CineHive.domain.report.service;
=======
package com.example.CineHive.domain.report;
>>>>>>> parent of 49bd7c6b ([Ref]: 도메인 패키지 구조 정리):CineHive/src/main/java/com/example/CineHive/domain/report/ReportService.java

import com.example.CineHive.domain.report.dto.ReportRequest;

/**
 * 사용자의 콘텐츠 신고 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface ReportService {

    /**
     * 특정 게시글을 신고합니다.
     *
     * @param postId        신고할 게시글의 ID
     * @param request       신고 내용(사유)을 담은 DTO
     * @param reporterEmail 신고하는 사용자의 이메일
     */
    void reportPost(Long postId, ReportRequest request, String reporterEmail);

    /**
     * 특정 댓글을 신고합니다.
     *
     * @param commentId     신고할 댓글의 ID
     * @param request       신고 내용(사유)을 담은 DTO
     * @param reporterEmail 신고하는 사용자의 이메일
     */
    void reportComment(Long commentId, ReportRequest request, String reporterEmail);
}
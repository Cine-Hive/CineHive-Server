package com.example.CineHive.service.post;

import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.post.CreatePostRequest;
import com.example.CineHive.dto.post.PostDetailResponse;
import com.example.CineHive.dto.post.PostSortType;
import com.example.CineHive.dto.post.PostSummaryResponse;
import com.example.CineHive.dto.post.UpdatePostRequest;

/**
 * 게시글(Post) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 */
public interface PostService {

    /**
     * 새로운 게시글을 생성합니다.
     * @param request 게시글 생성 요청 DTO
     * @param userEmail 작성자 이메일
     * @return 생성된 게시글 상세 정보
     */
    PostDetailResponse createPost(CreatePostRequest request, String userEmail);

    /**
     * 특정 ID의 게시글을 상세 조회합니다. 조회수가 1 증가합니다.
     * @param postId 조회할 게시글 ID
     * @return 조회된 게시글 상세 정보
     */
    PostDetailResponse getPostById(Long postId);

    /**
     * 특정 ID의 게시글을 수정합니다.
     * @param postId 수정할 게시글 ID
     * @param request 게시글 수정 요청 DTO
     * @param userEmail 수정 요청자 이메일
     * @return 수정된 게시글 상세 정보
     */
    PostDetailResponse updatePost(Long postId, UpdatePostRequest request, String userEmail);

    /**
     * 특정 ID의 게시글을 삭제합니다.
     * @param postId 삭제할 게시글 ID
     * @param userEmail 삭제 요청자 이메일
     */
    void deletePost(Long postId, String userEmail);

    /**
     * 게시글 목록을 페이징하여 조회합니다.
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 페이징된 게시글 요약 정보 목록
     */
    PagedResponse<PostSummaryResponse> getPosts(int page, int size, PostSortType sort);
}
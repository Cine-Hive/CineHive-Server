package com.example.CineHive.domain.post.service;

import com.example.CineHive.domain.common.service.DomainFinder;
import com.example.CineHive.domain.common.dto.PageResponse;
import com.example.CineHive.domain.post.dto.CreatePostRequest;
import com.example.CineHive.domain.post.dto.PostDetailResponse;
import com.example.CineHive.domain.post.dto.PostSummaryResponse;
import com.example.CineHive.domain.post.dto.UpdatePostRequest;
import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.CineHive.domain.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final DomainFinder domainFinder;

    @Override
    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, String userEmail) {
        log.info("새 게시글 작성을 시작합니다. 작성자: {}", userEmail);
        User user = domainFinder.findUserByEmail(userEmail);

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();
        Post savedPost = postRepository.save(post);

        log.info("게시글이 성공적으로 생성되었습니다. 게시글 ID: {}, 작성자: {}", savedPost.getId(), userEmail);
        return PostDetailResponse.from(savedPost);
    }

    @Override
    @Transactional
    public PostDetailResponse getPostById(Long postId) {
        log.debug("게시글 상세 조회를 시작합니다. 게시글 ID: {}", postId);

        int updatedRows = postRepository.incrementViews(postId);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        log.debug("게시글 조회수 증가 완료. 게시글 ID: {}", postId);

        Post post = domainFinder.findPostById(postId);
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void incrementViews(Long postId) {
        log.debug("게시글 조회수 1 증가를 시도합니다. 게시글 ID: {}", postId);
        int updatedRows = postRepository.incrementViews(postId);
        if (updatedRows == 0) {
            log.warn("조회수 증가 실패: 존재하지 않는 게시글 ID={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request, String userEmail) {
        log.info("게시글 수정을 시작합니다. 게시글 ID: {}, 요청자: {}", postId, userEmail);
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = findPostAndVerifyOwner(postId, user.getId());

        post.update(request.title(), request.content());
        log.info("게시글이 성공적으로 수정되었습니다. 게시글 ID: {}", postId);
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        log.info("게시글 삭제를 시작합니다. 게시글 ID: {}, 요청자: {}", postId, userEmail);
        User user = domainFinder.findUserByEmail(userEmail);

        int deletedRows = postRepository.deleteByIdAndUserId(postId, user.getId());
        if (deletedRows == 0) {
            if (!postRepository.existsById(postId)) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            } else {
                log.warn("게시글 삭제 권한 없음. 게시글 ID: {}, 요청자: {}", postId, userEmail);
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }
        log.info("게시글이 성공적으로 삭제되었습니다. ID: {}", postId);
    }

    @Override
    public PageResponse<PostSummaryResponse> getPosts(Pageable pageable) {
        log.debug("게시글 목록 조회를 시작합니다. Pageable: {}", pageable);
        Page<Post> postPage = postRepository.findAll(pageable);

        log.debug("{} 페이지에서 {}개의 게시글을 조회했습니다.", postPage.getNumber() + 1, postPage.getNumberOfElements());
        return PageResponse.from(postPage, PostSummaryResponse::from);
    }

    @Override
    public PageResponse<PostSummaryResponse> searchPosts(String keyword, Pageable pageable) {
        log.debug("게시글 키워드 검색을 시작합니다. 키워드: '{}', Pageable: {}", keyword, pageable);
        Page<Post> postPage = postRepository.searchByKeyword(keyword, pageable);

        log.debug("'{}' 키워드로 {} 페이지에서 {}개의 게시글을 조회했습니다.", keyword, postPage.getNumber() + 1, postPage.getNumberOfElements());
        return PageResponse.from(postPage, PostSummaryResponse::from);
    }

    private Post findPostAndVerifyOwner(Long postId, Long userId) {
        log.debug("게시글 소유권 검증을 시작합니다. 게시글 ID: {}, 사용자 ID: {}", postId, userId);
        Post post = domainFinder.findPostById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        log.debug("게시글 소유권 검증 완료. 게시글 ID: {}", postId);
        return post;
    }
}
package com.example.CineHive.domain.post;

import com.example.CineHive.domain.common.DomainFinder;
import com.example.CineHive.domain.common.dto.PagedResponse;
import com.example.CineHive.domain.post.dto.CreatePostRequest;
import com.example.CineHive.domain.post.dto.PostDetailResponse;
import com.example.CineHive.domain.post.dto.PostSortType;
import com.example.CineHive.domain.post.dto.PostSummaryResponse;
import com.example.CineHive.domain.post.dto.UpdatePostRequest;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();
        Post savedPost = postRepository.save(post);
        return PostDetailResponse.from(savedPost);
    }

    @Override
    @Transactional
    public PostDetailResponse getPostById(Long postId) {
        int updatedRows = postRepository.incrementViews(postId);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        Post post = domainFinder.findPostById(postId);
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void incrementViews(Long postId) {
        int updatedRows = postRepository.incrementViews(postId);
        if (updatedRows == 0) {
            log.warn("조회수 증가 실패: 존재하지 않는 게시글 ID={}", postId);
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = findPostAndVerifyOwner(postId, user.getId());

        post.update(request.title(), request.content());
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);

        int deletedRows = postRepository.deleteByIdAndUserId(postId, user.getId());
        if (deletedRows == 0) {
            if (!postRepository.existsById(postId)) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            } else {
                throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
            }
        }
        log.info("게시글이 성공적으로 삭제되었습니다. ID: {}", postId);
    }

    @Override
    public PagedResponse<PostSummaryResponse> getPosts(int page, int size, PostSortType sort) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort.getDbField()));
        Page<Post> postPage = postRepository.findAll(pageable);

        return new PagedResponse<>(
                postPage.getContent().stream().map(PostSummaryResponse::from).toList(),
                postPage.getNumber() + 1,
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    @Override
    public PagedResponse<PostSummaryResponse> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> postPage = postRepository.searchByKeyword(keyword, pageable);

        return new PagedResponse<>(
                postPage.getContent().stream().map(PostSummaryResponse::from).toList(),
                postPage.getNumber() + 1,
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.isLast()
        );
    }

    /**
     * 게시글을 조회하고 소유권을 검증합니다.
     * DomainFinder를 통해 게시글을 먼저 조회하고, 그 후에 소유권을 비교하여 불필요한 DB 호출을 제거합니다.
     * @param postId 조회할 게시글 ID
     * @param userId 검증할 사용자 ID
     * @return 검증된 Post 엔티티
     */
    private Post findPostAndVerifyOwner(Long postId, Long userId) {
        Post post = domainFinder.findPostById(postId);
        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return post;
    }
}
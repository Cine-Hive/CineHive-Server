package com.example.CineHive.domain.post;

import com.example.CineHive.domain.common.dto.PagedResponse;
import com.example.CineHive.domain.post.dto.CreatePostRequest;
import com.example.CineHive.domain.post.dto.PostDetailResponse;
import com.example.CineHive.domain.post.dto.PostSortType;
import com.example.CineHive.domain.post.dto.PostSummaryResponse;
import com.example.CineHive.domain.post.dto.UpdatePostRequest;
import com.example.CineHive.domain.user.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostDetailResponse createPost(CreatePostRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .user(user)
                .build();
        Post savedPost = postRepository.save(post);
        return PostDetailResponse.from(savedPost);
    }

    @Override
    public PostDetailResponse getPostById(Long postId) {
        Post post = findPostById(postId);
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void incrementViews(Long postId) {
        int updatedRows = postRepository.incrementViews(postId);
        if (updatedRows == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostAndVerifyOwner(postId, user.getId());

        post.update(request.title(), request.content());
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostAndVerifyOwner(postId, user.getId());

        postRepository.delete(post);
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

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    /**
     * 게시글을 조회하고 소유권을 검증합니다.
     * 한 번의 쿼리로 조회와 검증을 시도하고, 실패 시 원인을 명확히 구분하여 예외를 발생시킵니다.
     * @param postId 조회할 게시글 ID
     * @param userId 검증할 사용자 ID
     * @return 검증된 Post 엔티티
     */
    private Post findPostAndVerifyOwner(Long postId, Long userId) {
        return postRepository.findByIdAndUserId(postId, userId)
                .orElseThrow(() -> {
                    if (postRepository.existsById(postId)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
                    } else {
                        throw new BusinessException(ErrorCode.POST_NOT_FOUND);
                    }
                });
    }
}

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
    private final DomainFinder domainFinder; // UserRepository 대신 DomainFinder 주입

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
    public PostDetailResponse getPostById(Long postId) {
        Post post = domainFinder.findPostById(postId);
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
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = findPostAndVerifyOwner(postId, user.getId());

        post.update(request.title(), request.content());
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
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

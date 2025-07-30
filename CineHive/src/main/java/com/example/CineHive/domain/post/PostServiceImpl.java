package com.example.CineHive.domain.post;

import com.example.CineHive.global.common.dto.PagedResponse;
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
    @Transactional
    public PostDetailResponse getPostById(Long postId) {
        Post post = findPostById(postId);
        post.increaseViews(); // Dirty Checking에 의해 update 쿼리 발생
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        verifyPostOwnership(post, user);

        post.update(request.title(), request.content());
        return PostDetailResponse.from(post);
    }

    @Override
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        verifyPostOwnership(post, user);

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

    private void verifyPostOwnership(Post post, User user) {
        if (!post.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }
}

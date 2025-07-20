package com.example.CineHive.service.post;

import com.example.CineHive.dto.global.PagedResponse;
import com.example.CineHive.dto.post.CreatePostRequest;
import com.example.CineHive.dto.post.PostDetailResponse;
import com.example.CineHive.dto.post.PostSortType;
import com.example.CineHive.dto.post.PostSummaryResponse;
import com.example.CineHive.dto.post.UpdatePostRequest;
import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.mapper.post.PostMapper;
import com.example.CineHive.repository.post.PostRepository;
import com.example.CineHive.repository.user.UserRepository;
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
        return PostMapper.toDetailResponse(savedPost);
    }

    @Override
    @Transactional
    public PostDetailResponse getPostById(Long postId) {
        Post post = findPostById(postId);
        post.increaseViews();
        return PostMapper.toDetailResponse(post);
    }

    @Override
    @Transactional
    public PostDetailResponse updatePost(Long postId, UpdatePostRequest request, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        verifyPostOwnership(post, user);

        post.update(request.title(), request.content());
        return PostMapper.toDetailResponse(post);
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
        // 클라이언트가 1페이지를 요청하면, Spring Data JPA는 0페이지를 조회해야 합니다.
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, sort.getDbField()));
        Page<Post> postPage = postRepository.findAll(pageable);

        return new PagedResponse<>(
                postPage.getContent().stream().map(PostMapper::toSummaryResponse).toList(),
                postPage.getNumber(),
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
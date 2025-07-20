package com.example.CineHive.service.post;

import com.example.CineHive.entity.post.Bookmark;
import com.example.CineHive.entity.post.Post;
import com.example.CineHive.entity.user.User;
import com.example.CineHive.exception.BusinessException;
import com.example.CineHive.exception.ErrorCode;
import com.example.CineHive.repository.post.BookmarkRepository;
import com.example.CineHive.repository.post.PostRepository;
import com.example.CineHive.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void addBookmark(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        if (bookmarkRepository.existsByUserAndPost(user, post)) {
            throw new BusinessException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .post(post)
                .build();
        bookmarkRepository.save(bookmark);

        post.increaseBookmarkCount();
        log.info("User {} bookmarked post {}", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeBookmark(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);

        Bookmark bookmark = bookmarkRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));

        bookmarkRepository.delete(bookmark);

        post.decreaseBookmarkCount();
        log.info("User {} removed bookmark from post {}", user.getId(), post.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public int getBookmarkCount(Long postId) {
        Post post = findPostById(postId);
        return post.getBookmarkCount();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarkedByUser(Long postId, String userEmail) {
        User user = findUserByEmail(userEmail);
        Post post = findPostById(postId);
        return bookmarkRepository.existsByUserAndPost(user, post);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
package com.example.CineHive.domain.post.bookmark.service;

import com.example.CineHive.domain.post.bookmark.entity.Bookmark;
import com.example.CineHive.domain.post.bookmark.repository.BookmarkRepository;
import com.example.CineHive.global.util.DomainFinder;
import com.example.CineHive.domain.post.entity.Post;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final PostRepository postRepository;
    private final DomainFinder domainFinder;

    @Override
    @Transactional
    public void addBookmark(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = domainFinder.findPostById(postId);

        if (bookmarkRepository.existsByUserIdAndPostId(user.getId(), post.getId())) {
            log.debug("사용자 ID: {}가 이미 게시글 ID: {}를 북마크했습니다.", user.getId(), post.getId());
            throw new BusinessException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
        }

        bookmarkRepository.save(Bookmark.builder().user(user).post(post).build());
        if (postRepository.increaseBookmarkCount(postId) == 0) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        log.info("사용자 ID: {}가 게시글 ID: {}를 북마크했습니다.", user.getId(), post.getId());
    }

    @Override
    @Transactional
    public void removeBookmark(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Post post = domainFinder.findPostById(postId);

        int deleted = bookmarkRepository.deleteByUserAndPost(user, post);
        if (deleted == 0) {
            throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
        }

        if (postRepository.decreaseBookmarkCount(postId) == 0) {
            log.warn("북마크 수 감소 실패: 존재하지 않는 게시글(ID: {})에 대한 동시성 문제 가능성", postId);
        }
        log.info("사용자 ID: {}가 게시글 ID: {}의 북마크를 취소했습니다.", user.getId(), post.getId());
    }

    @Override
    public int getBookmarkCount(Long postId) {
        domainFinder.findPostById(postId);
        return bookmarkRepository.countByPost_Id(postId);
    }

    @Override
    public boolean isBookmarkedByUser(Long postId, String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        return bookmarkRepository.existsByUserIdAndPostId(user.getId(), postId);
    }
}

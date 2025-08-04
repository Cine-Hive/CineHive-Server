package com.example.CineHive.domain.media.service;

import com.example.CineHive.domain.media.entity.Media;
import com.example.CineHive.domain.media.entity.MediaLike;
import com.example.CineHive.domain.media.repository.MediaLikeRepository;
import com.example.CineHive.domain.media.repository.MediaRepository;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.service.AbstractLikeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MediaLikeServiceImpl extends AbstractLikeService<Media, MediaLike> implements MediaLikeService {

    private final MediaRepository mediaRepository;
    private final MediaLikeRepository mediaLikeRepository;

    public MediaLikeServiceImpl(UserRepository userRepository, MediaRepository mediaRepository, MediaLikeRepository mediaLikeRepository) {
        super(userRepository);
        this.mediaRepository = mediaRepository;
        this.mediaLikeRepository = mediaLikeRepository;
    }

    @Override
    protected JpaRepository<Media, Long> getTargetRepository() {
        return this.mediaRepository;
    }

    @Override
    protected boolean isAlreadyLiked(User user, Media media) {
        return mediaLikeRepository.existsByUserAndMedia(user, media);
    }

    @Override
    protected MediaLike createLikeEntity(User user, Media media) {
        return MediaLike.builder().user(user).media(media).build();
    }

    @Override
    @Transactional
    protected void saveLike(MediaLike mediaLike) {
        mediaLikeRepository.save(mediaLike);
        if (mediaRepository.increaseLikeCount(mediaLike.getMedia().getId()) == 0) {
            throw new BusinessException(ErrorCode.MEDIA_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    protected void deleteLike(User user, Media media) {
        MediaLike mediaLike = mediaLikeRepository.findByUserAndMedia(user, media)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIKE_NOT_FOUND));
        mediaLikeRepository.delete(mediaLike);
        if (mediaRepository.decreaseLikeCount(media.getId()) == 0) {
            log.warn("좋아요 카운트 감소 실패: 미디어를 찾을 수 없습니다. Media ID: {}", media.getId());
        }
    }
}
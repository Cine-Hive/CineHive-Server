package com.example.CineHive.global.service;

import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.entity.BaseEntity;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * '좋아요' 서비스의 공통 로직을 담은 추상 클래스입니다. (템플릿 메서드 패턴)
 * @param <T> '좋아요'의 대상이 되는 엔티티 타입 (예: Post, Person)
 * @param <L> '좋아요' 자체를 나타내는 엔티티 타입 (예: PostLike, PersonLike)
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLikeService<T extends BaseEntity, L> implements LikeService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void like(String userEmail, Long targetId) {
        User user = findUserByEmail(userEmail);
        T target = findTargetById(targetId);

        if (isAlreadyLiked(user, target)) {
            log.debug("이미 '좋아요'한 대상입니다. User: {}, Target: {} {}", userEmail, target.getClass().getSimpleName(), targetId);
            return;
        }

        L likeEntity = createLikeEntity(user, target);
        saveLike(likeEntity);
        log.info("'좋아요'가 등록되었습니다. User: {}, Target: {} {}", userEmail, target.getClass().getSimpleName(), targetId);
    }

    @Override
    @Transactional
    public void unlike(String userEmail, Long targetId) {
        User user = findUserByEmail(userEmail);
        T target = findTargetById(targetId);

        deleteLike(user, target);
        log.info("'좋아요'가 취소되었습니다. User: {}, Target: {} {}", userEmail, target.getClass().getSimpleName(), targetId);
    }

    /**
     * '좋아요' 대상 엔티티를 조회합니다.
     * 기본 구현은 ID로 찾지만, 자식 클래스에서 'findOrCreate' 로직으로 재정의할 수 있습니다.
     * @param targetId '좋아요' 대상의 ID
     * @return 조회된 대상 엔티티
     */
    protected T findTargetById(Long targetId) {
        return getTargetRepository().findById(targetId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TARGET_NOT_FOUND));
    }

    /**
     * 대상 엔티티를 조회하기 위한 Repository를 반환합니다.
     * @return JpaRepository<T, Long>
     */
    protected abstract JpaRepository<T, Long> getTargetRepository();

    /**
     * 이미 '좋아요'를 눌렀는지 확인합니다.
     * @param user 사용자 엔티티
     * @param target '좋아요' 대상 엔티티
     * @return 좋아요 여부
     */
    protected abstract boolean isAlreadyLiked(User user, T target);

    /**
     * '좋아요' 엔티티를 생성합니다.
     * @param user 사용자 엔티티
     * @param target '좋아요' 대상 엔티티
     * @return 생성된 '좋아요' 엔티티
     */
    protected abstract L createLikeEntity(User user, T target);

    /**
     * '좋아요' 엔티티를 저장소에 저장합니다.
     * @param likeEntity 저장할 '좋아요' 엔티티
     */
    protected abstract void saveLike(L likeEntity);

    /**
     * '좋아요' 엔티티를 삭제합니다.
     * @param user 사용자 엔티티
     * @param target '좋아요' 대상 엔티티
     */
    protected abstract void deleteLike(User user, T target);

    private User findUserByEmail(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
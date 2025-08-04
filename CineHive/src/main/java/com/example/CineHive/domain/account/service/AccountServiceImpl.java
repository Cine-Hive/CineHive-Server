package com.example.CineHive.domain.account.service;

import com.example.CineHive.domain.account.dto.AccountInfoResponse;
import com.example.CineHive.domain.account.dto.UpdatePasswordRequest;
import com.example.CineHive.domain.account.dto.UpdatePreferencesRequest;
import com.example.CineHive.domain.account.dto.UpdateProfileRequest;
import com.example.CineHive.domain.auth.password.entity.PasswordHistory;
import com.example.CineHive.domain.auth.password.repository.PasswordHistoryRepository;
import com.example.CineHive.domain.media.enums.Genre;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.properties.SecurityPolicyProperties;
import com.example.CineHive.global.util.DomainFinder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final DomainFinder domainFinder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final SecurityPolicyProperties securityPolicy;

    @Override
    public AccountInfoResponse getAccountInfo(String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, UpdatePasswordRequest request) {
        User user = domainFinder.findUserByEmail(userEmail);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        validatePasswordHistory(user, request.newPassword());
        archiveOldPassword(user);
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("사용자(ID:{})가 비밀번호를 변경했습니다.", user.getId());
    }

    @Override
    @Transactional
    public AccountInfoResponse updateProfile(String userEmail, UpdateProfileRequest request) {
        User user = domainFinder.findUserByEmail(userEmail);
        if (request.nickname() != null) {
            if (userRepository.existsByNicknameAndIdNot(request.nickname(), user.getId())) {
                throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
            }
            user.changeNickname(request.nickname());
            log.info("사용자(ID:{})가 프로필을 수정했습니다. (닉네임 변경)", user.getId());
        }
        // TODO: 추후 bio, profileImageUrl 등 다른 프로필 정보 업데이트 로직 추가
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public AccountInfoResponse updatePreferences(String userEmail, UpdatePreferencesRequest request) {
        User user = domainFinder.findUserByEmail(userEmail);
        try {
            // DTO에 @NotNull이 있으므로, null 체크는 불필요
            Set<Genre> newGenres = request.genres().stream()
                    .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                    .collect(Collectors.toSet());
            user.updateGenres(newGenres);
            log.info("사용자(ID:{})가 선호 설정을 수정했습니다. (장르 변경)", user.getId());
        } catch (IllegalArgumentException e) {
            // 예외 발생 시, 어떤 값 때문에 문제가 생겼는지 로그를 남겨 디버깅 용이성 확보
            log.warn("잘못된 장르 이름으로 업데이트 시도. 사용자 ID: {}, 요청된 장르: {}", user.getId(), request.genres());
            // ErrorCode에 정의된 메시지를 사용하도록 통일
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        // TODO: 추후 알림 설정 등 다른 선호 정보 업데이트 로직 추가
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        Long userId = user.getId();
        log.warn("사용자(ID:{})의 회원 탈퇴 절차를 시작합니다.", userId);

        user.anonymize();
        userRepository.delete(user);

        // TODO: 탈퇴한 사용자의 Refresh Token도 삭제하는 로직 추가 (RefreshTokenRepository.deleteById(userEmail))
        // TODO: 탈퇴 이벤트(UserDeactivatedEvent)를 발행하여, 관련 데이터(게시글, 댓글 등)를 비동기적으로 처리하는 리스너 구현

        log.info("사용자(ID:{}) 계정이 성공적으로 비활성화(소프트 삭제)되었습니다.", userId);
    }

    private void validatePasswordHistory(User user, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        history.stream()
                .limit(securityPolicy.getPassword().getHistorySize())
                .forEach(record -> {
                    if (passwordEncoder.matches(newPassword, record.getPasswordHash())) {
                        throw new BusinessException(ErrorCode.PASSWORD_REUSE_PROHIBITED);
                    }
                });
    }

    private void archiveOldPassword(User user) {
        PasswordHistory newHistoryRecord = new PasswordHistory(user, user.getPassword());
        passwordHistoryRepository.save(newHistoryRecord);
        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        if (history.size() > securityPolicy.getPassword().getHistorySize()) {
            PasswordHistory oldestRecord = history.get(history.size() - 1);
            passwordHistoryRepository.delete(oldestRecord);
        }
    }
}

package com.example.CineHive.domain.account.controller.entity;

import com.example.CineHive.domain.auth.controller.PasswordHistory;
import com.example.CineHive.domain.auth.controller.PasswordHistoryRepository;
import com.example.CineHive.domain.common.controller.DomainFinder;
import com.example.CineHive.domain.media.controller.Genre;
import com.example.CineHive.domain.user.controller.User;
import com.example.CineHive.domain.user.controller.UserRepository;
import com.example.CineHive.domain.user.dto.AccountInfoResponse;
import com.example.CineHive.domain.user.dto.UpdateGenresRequest;
import com.example.CineHive.domain.user.dto.UpdateNicknameRequest;
import com.example.CineHive.domain.user.dto.UpdatePasswordRequest;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.security.password.history-size}")
    private int passwordHistorySize;

    @Override
    public AccountInfoResponse getAccountInfo(String email) {
        User user = domainFinder.findUserByEmail(email);
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public void changeNickname(String email, UpdateNicknameRequest request) {
        User user = domainFinder.findUserByEmail(email);
        String newNickname = request.nickname();
        if (userRepository.existsByNickname(newNickname) && !user.getNickname().equals(newNickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.changeNickname(newNickname);
        log.info("사용자({}), 닉네임을 '{}'(으)로 변경했습니다.", email, newNickname);
    }

    @Override
    @Transactional
    public void changePassword(String email, UpdatePasswordRequest request) {
        User user = domainFinder.findUserByEmail(email);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String newPassword = request.newPassword();

        validatePasswordHistory(user, newPassword);
        archiveOldPassword(user);
        user.changePassword(passwordEncoder.encode(newPassword));
        log.info("사용자({}), 비밀번호를 변경했습니다.", email);
    }

    @Override
    @Transactional
    public void updateGenres(String email, UpdateGenresRequest request) {
        User user = domainFinder.findUserByEmail(email);
        Set<Genre> newGenres = request.genres().stream()
                .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                .collect(Collectors.toSet());
        user.updateGenres(newGenres);
        log.info("사용자({}), 선호 장르를 업데이트했습니다.", email);
    }



    @Override
    @Transactional
    public void deleteAccount(String email) {
        log.warn("사용자({})의 모든 데이터를 삭제합니다. (회원 탈퇴)", email);
        User user = domainFinder.findUserByEmail(email);
        userRepository.delete(user);
        log.info("사용자({}) 계정이 성공적으로 삭제되었습니다.", email);
    }

    /**
     * 새로운 비밀번호가 과거에 사용된 비밀번호와 일치하는지 검증합니다.
     * @param user 검증할 사용자
     * @param newPassword 새로운 비밀번호 (평문)
     */
    private void validatePasswordHistory(User user, String newPassword) {
        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);

        history.stream()
                .limit(passwordHistorySize)
                .forEach(record -> {
                    if (passwordEncoder.matches(newPassword, record.getPasswordHash())) {
                        throw new BusinessException(ErrorCode.PASSWORD_REUSE_PROHIBITED);
                    }
                });
    }

    /**
     * 현재 비밀번호를 히스토리 테이블에 저장하고, 설정된 개수를 초과하는 가장 오래된 기록은 삭제합니다.
     * @param user 비밀번호를 기록할 사용자
     */
    private void archiveOldPassword(User user) {
        PasswordHistory newHistoryRecord = new PasswordHistory(user, user.getPassword());
        passwordHistoryRepository.save(newHistoryRecord);

        List<PasswordHistory> history = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        if (history.size() > passwordHistorySize) {
            PasswordHistory oldestRecord = history.get(history.size() - 1);
            passwordHistoryRepository.delete(oldestRecord);
        }
    }
}
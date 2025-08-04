package com.example.CineHive.domain.account.service;

import com.example.CineHive.domain.account.dto.AccountInfoResponse;
import com.example.CineHive.domain.account.dto.UpdatePasswordRequest;
import com.example.CineHive.domain.account.dto.UpdatePreferencesRequest;
import com.example.CineHive.domain.account.dto.UpdateProfileRequest;
import com.example.CineHive.domain.auth.password.entity.PasswordHistory;
import com.example.CineHive.domain.auth.password.repository.PasswordHistoryRepository;
import com.example.CineHive.domain.media.Genre;
import com.example.CineHive.domain.user.entity.User;
import com.example.CineHive.domain.user.repository.UserRepository;
import com.example.CineHive.global.exception.BusinessException;
import com.example.CineHive.global.exception.ErrorCode;
import com.example.CineHive.global.properties.SecurityPolicyProperties;
import com.example.CineHive.global.util.DomainFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        }
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public AccountInfoResponse updatePreferences(String userEmail, UpdatePreferencesRequest request) {
        User user = domainFinder.findUserByEmail(userEmail);
        try {
            Set<Genre> newGenres = request.genres().stream()
                    .map(genreName -> Genre.valueOf(genreName.toUpperCase()))
                    .collect(Collectors.toSet());
            user.updateGenres(newGenres);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("유효하지 않은 장르 이름이 포함되어 있습니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
        return AccountInfoResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteAccount(String userEmail) {
        User user = domainFinder.findUserByEmail(userEmail);
        userRepository.delete(user);
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
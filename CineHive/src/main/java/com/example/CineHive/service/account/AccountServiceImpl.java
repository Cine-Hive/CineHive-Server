package com.example.CineHive.service.account;

import com.example.CineHive.dto.account.AccountInfoResponseDto;
import com.example.CineHive.entity.member.Member;
import com.example.CineHive.repository.board.*;
import com.example.CineHive.repository.member.MemberRepository;
import com.example.CineHive.repository.reply.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 탈퇴 시 연관 데이터 삭제를 위한 리포지토리들
    private final ReplyLikeRepository replyLikeRepository;
    private final ReplyDislikeRepository replyDislikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final DisLikeRepository disLikeRepository;
    private final LikeRepository likeRepository;
    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final ReplyBookmarkRepository replyBookmarkRepository;

    @Override
    public AccountInfoResponseDto getAccountInfo(String email) {
        Member member = findMemberByEmail(email);
        return AccountInfoResponseDto.from(member);
    }

    @Override
    @Transactional
    public void changeNickname(String email, String newNickname) {
        Member member = findMemberByEmail(email);
        // 자기 자신의 닉네임으로 변경하는 경우는 예외처리하지 않음
        if (memberRepository.existsByNickname(newNickname) && !member.getNickname().equals(newNickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        member.changeNickname(newNickname);
        log.info("Member {} changed nickname to {}", email, newNickname);
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        Member member = findMemberByEmail(email);
        if (!passwordEncoder.matches(oldPassword, member.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }
        member.changePassword(passwordEncoder.encode(newPassword));
        log.info("Member {} changed password.", email);
    }

    @Override
    @Transactional
    public void updateGenres(String email, List<String> genres) {
        Member member = findMemberByEmail(email);
        member.updateGenres(new HashSet<>(genres));
        log.info("Member {} updated genres.", email);
    }

    @Override
    @Transactional
    public void deleteAccount(String email) {
        log.warn("Deleting all data for member: {}", email);

        // 연관된 모든 데이터 삭제
        // 실제 서비스에서는 성능과 데이터 무결성을 위해 '소프트 삭제'나 비동기 처리를 고려해야 합니다.
        replyLikeRepository.deleteByMember_Email(email);
        replyDislikeRepository.deleteByMember_Email(email);
        bookmarkRepository.deleteByMember_Email(email);
        replyBookmarkRepository.deleteByMember_Email(email);
        likeRepository.deleteByMember_Email(email);
        disLikeRepository.deleteByMember_Email(email);
        commentRepository.deleteByMember_Email(email);
        replyRepository.deleteByMember_Email(email);
        boardRepository.deleteByMember_Email(email);

        // 마지막으로 회원 정보 삭제
        memberRepository.deleteByEmail(email);
        log.info("Successfully deleted account for member: {}", email);
    }

    /**
     * 이메일로 회원을 조회하는 내부 헬퍼 메서드.
     * @param email 조회할 이메일
     * @return Member 엔티티
     * @throws IllegalArgumentException 해당 이메일의 회원이 없을 경우
     */
    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}
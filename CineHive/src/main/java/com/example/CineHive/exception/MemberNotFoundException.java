package com.example.CineHive.exception;

/**
 * 회원을 찾을 수 없을 때 발생하는 예외입니다.
 */
public class MemberNotFoundException extends BusinessException {

    /**
     * 이메일을 기반으로 회원을 찾지 못했을 경우 사용하는 생성자입니다.
     * @param email 찾지 못한 회원의 이메일
     */
    public MemberNotFoundException(String email) {
        super(String.format("해당 이메일의 회원을 찾을 수 없습니다: %s", email), ErrorCode.MEMBER_NOT_FOUND);
    }

    /**
     * ID를 기반으로 회원을 찾지 못했을 경우 사용하는 생성자입니다.
     * @param id 찾지 못한 회원의 ID
     */
    public MemberNotFoundException(Long id) {
        super(String.format("해당 ID의 회원을 찾을 수 없습니다: %d", id), ErrorCode.MEMBER_NOT_FOUND);
    }
}
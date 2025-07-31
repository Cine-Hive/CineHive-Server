package com.example.CineHive.global.validation;

/**
 * 애플리케이션 전반에서 사용되는 유효성 검증 정규표현식을 상수로 정의한 클래스입니다.
 * 인스턴스화할 수 없습니다.
 */
public final class ValidationPatterns {

    // private 생성자로 인스턴스화 방지
    private ValidationPatterns() {}

    /** 비밀번호: 영문, 숫자, 특수문자(@$!%*?&) 포함 8~20자 */
    public static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";

    /** 이름: 한글 또는 영문 2~30자 */
    public static final String NAME_PATTERN = "^[a-zA-Z가-힣]{2,30}$";

    /** 닉네임: 공백/특수문자 제외 한글, 영문, 숫자 2~10자 */
    public static final String NICKNAME_PATTERN = "^[a-zA-Z0-9가-힣]{2,10}$";
}
package com.example.CineHive.config.converter;

/**
 * 클라이언트로부터 받은 문자열 값을 Enum 상수로 변환할 수 있는 모든 Enum이 구현해야 하는 인터페이스.
 */
public interface StringValueConvertible {
    String getClientValue();
}
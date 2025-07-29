package com.example.CineHive.global.config.web.converter;

import com.example.CineHive.domain.oauth.ProviderType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToProviderTypeConverter implements Converter<String, ProviderType> {

    @Override
    public ProviderType convert(String source) {
        if (source == null) {
            return null;
        }
        try {
            // 입력된 문자열을 대문자로 변환하여 Enum으로 변환 시도
            return ProviderType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 유효하지 않은 값이 들어올 경우 null 반환 또는 예외 처리
            // 여기서는 null을 반환하여 Spring의 기본 처리에 맡깁니다.
            return null;
        }
    }
}

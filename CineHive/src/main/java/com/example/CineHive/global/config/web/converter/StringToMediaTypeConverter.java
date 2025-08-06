package com.example.CineHive.global.config.web.converter;

import com.example.CineHive.domain.media.enums.MediaType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * HTTP 요청 파라미터(String)를 MediaType Enum으로 변환하는 커스텀 컨버터입니다.
 */
@Component // Spring이 컴포넌트로 인식하도록 어노테이션 추가
public class StringToMediaTypeConverter implements Converter<String, MediaType> {

    @Override
    public MediaType convert(String source) {
        // 이미 강력하게 만들어진 fromString 메서드를 그대로 사용합니다.
        // source가 null일 경우 Spring에서 처리하므로 여기서 별도 null 체크는 불필요합니다.
        return MediaType.fromString(source);
    }
}
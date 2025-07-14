package com.example.CineHive.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * String을 StringValueConvertible 인터페이스를 구현한 Enum으로 변환하는 제네릭 컨버터 팩토리.
 * @Component 어노테이션을 통해 Spring Bean으로 등록됩니다.
 */
@Component
public class StringToEnumConverterFactory implements ConverterFactory<String, Enum<? extends StringValueConvertible>> {

    @Override
    public <T extends Enum<? extends StringValueConvertible>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter<>(targetType);
    }

    private static final class StringToEnumConverter<T extends Enum<? extends StringValueConvertible>> implements Converter<String, T> {

        private final Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }

            return Arrays.stream(enumType.getEnumConstants())
                    .filter(e -> ((StringValueConvertible) e).getClientValue().equalsIgnoreCase(source.trim()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 Enum 값입니다: " + source));
        }
    }
}
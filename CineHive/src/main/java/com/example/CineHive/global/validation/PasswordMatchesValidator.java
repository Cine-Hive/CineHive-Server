package com.example.CineHive.global.validation;

import com.example.CineHive.domain.auth.dto.RegisterRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @PasswordMatches 어노테이션의 실제 유효성 검증 로직을 구현하는 클래스입니다.
 * 검증 실패 시, 에러 메시지를 'confirmPassword' 필드에 바인딩합니다.
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegisterRequest> {

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        // 초기화가 필요하지 않으므로 비워둡니다.
    }

    @Override
    public boolean isValid(RegisterRequest request, ConstraintValidatorContext context) {
        boolean isValid = request.password() != null && request.password().equals(request.confirmPassword());

        if (!isValid) {
            context.disableDefaultConstraintViolation();

            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }

        return isValid;
    }
}
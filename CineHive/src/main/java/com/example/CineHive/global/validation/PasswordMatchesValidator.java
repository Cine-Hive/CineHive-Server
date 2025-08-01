package com.example.CineHive.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

/**
 * @PasswordMatches 어노테이션의 실제 유효성 검증 로직을 구현하는 클래스입니다.
 * 지정된 두 필드의 값이 일치하는지 동적으로 확인합니다.
 */
public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private String passwordFieldName;
    private String confirmPasswordFieldName;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        this.passwordFieldName = constraintAnnotation.passwordField();
        this.confirmPasswordFieldName = constraintAnnotation.confirmPasswordField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(value);
        Object passwordValue = beanWrapper.getPropertyValue(passwordFieldName);
        Object confirmPasswordValue = beanWrapper.getPropertyValue(confirmPasswordFieldName);

        boolean isValid = (passwordValue != null) && passwordValue.equals(confirmPasswordValue);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(confirmPasswordFieldName) // 에러를 'confirmPassword' 필드에 지정
                    .addConstraintViolation();
        }

        return isValid;
    }
}
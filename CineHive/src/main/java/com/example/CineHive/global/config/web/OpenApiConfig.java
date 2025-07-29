package com.example.CineHive.global.config.web; // 패키지 경로는 프로젝트에 맞게 조정하세요.

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc-OpenAPI(Swagger) 관련 설정을 위한 클래스
 */
@Configuration
public class OpenApiConfig {

    /**
     * Swagger UI에 JWT 인증을 위한 'Authorize' 버튼을 추가하고,
     * API 명세의 전역적인 설정을 커스터마이징합니다.
     *
     * @return 커스터마이징된 OpenAPI 객체
     */
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("CineHive API Documentation")
                .version("v1.0.0")
                .description("영화 커뮤니티 CineHive 프로젝트의 API 명세서입니다.");

        String securitySchemeName = "bearerAuth";
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme))
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}
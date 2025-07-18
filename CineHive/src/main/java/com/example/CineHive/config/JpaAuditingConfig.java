package com.example.CineHive.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 기능을 활성화하는 설정 클래스입니다.
 * @EnableJpaAuditing 어노테이션을 메인 클래스에서 분리하여
 * 테스트 시 불필요한 JPA 관련 Bean들이 로드되는 것을 방지합니다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
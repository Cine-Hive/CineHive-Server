package com.example.CineHive.config;

import com.example.CineHive.filter.JwtRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    // Swagger UI 및 API 문서 접근을 위한 경로
    private static final String[] SWAGGER_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (Stateless JWT 방식에서는 불필요)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. 세션 관리 정책을 STATELESS로 설정 (세션을 사용하지 않음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. HTTP 요청에 대한 인가 규칙 설정
                .authorizeHttpRequests(authz -> authz
                        // --- 인증 없이 접근 허용 (Permit All) ---
                        .requestMatchers(SWAGGER_PATHS).permitAll() // Swagger 경로 허용
                        .requestMatchers("/api/v1/members/register").permitAll() // 회원가입
                        .requestMatchers("/api/v1/members/login").permitAll()    // 일반 로그인
                        .requestMatchers("/api/v1/members/check-email", "/api/v1/members/check-nickname").permitAll() // 중복 확인
                        .requestMatchers("/api/v1/oauth2/**").permitAll() // 모든 OAuth2 관련 요청

                        // 미디어, 배너 조회 등 GET 요청은 대부분 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/banners").permitAll()

                        // 게시글/댓글 목록 조회, 상세 조회 등 GET 요청 허용
                        .requestMatchers(HttpMethod.GET, "/api/v1/boards", "/api/v1/boards/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/boards/{boardId}/comments").permitAll()

                        // --- 특정 역할(Role) 필요 ---
                        // 관리자 API 경로는 ADMIN 역할 필요 (hasRole은 'ROLE_' 접두사를 자동으로 추가해줌)
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // --- 인증만 되면 접근 가능 (Authenticated) ---
                        // 그 외 모든 요청은 인증된 사용자만 접근 가능
                        .anyRequest().authenticated()
                )

                // 5. 직접 만든 JWT 필터를 Spring Security 필터 체인에 추가
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder Bean을 등록합니다.
     * BCrypt 알고리즘을 사용합니다.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정을 위한 Bean을 등록합니다.
     * 다른 도메인에서의 요청을 허용하는 정책을 정의합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 출처(Origin) 설정
        config.setAllowedOrigins(List.of("http://localhost:8080", "http://localhost:8081", "https://cinehive.com"));
        // 허용할 HTTP 메서드 설정
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 허용할 HTTP 헤더 설정
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        // 자격 증명(쿠키 등) 허용 여부 설정
        config.setAllowCredentials(true);
        // Pre-flight 요청의 캐시 시간(초) 설정
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로("/**")에 대해 위에서 정의한 CORS 정책을 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
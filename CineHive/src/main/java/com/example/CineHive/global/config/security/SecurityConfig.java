package com.example.CineHive.global.config.security;

import com.example.CineHive.global.jwt.JwtAuthenticationFilter;
import com.example.CineHive.global.security.CustomAuthenticationEntryPoint;
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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // --- URL 패턴 상수화 ---
    private static final String[] AUTH_WHITELIST = {
            // Swagger UI
            "/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/webjars/**",
            // 인증 API
            "/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/reissue",
            "/api/v1/auth/check-email", "/api/v1/auth/check-nickname",
            // OAuth2
            "/api/v1/oauth2/**"
    };

    private static final String[] PUBLIC_GET_PATHS = {
            // 검색 API
            "/api/v1/search/**",
            // 미디어, 배너, 게시글, 댓글 등 조회 API
            "/api/v1/media/**", "/api/v1/banners", "/api/v1/posts", "/api/v1/posts/**",
            "/api/v1/posts/{postId}/comments"
    };

    private static final String[] ACTUATOR_PUBLIC_PATHS = {
            // Actuator 상태 체크
            "/actuator/health", "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                )

                .authorizeHttpRequests(authz -> authz
                        // --- 인증 없이 접근 허용 (Permit All) ---
                        .requestMatchers(AUTH_WHITELIST).permitAll()
                        .requestMatchers(ACTUATOR_PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.GET, PUBLIC_GET_PATHS).permitAll()

                        // --- 특정 역할(Role)이 필요한 경우 ---
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/ops/batch/**").hasRole("ADMIN")
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // --- 그 외 모든 요청은 인증 필요 ---
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://localhost:8081", "https://cinehive.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

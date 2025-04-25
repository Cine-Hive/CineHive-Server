package com.example.CineHive.config;

import com.example.CineHive.filter.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            // 관리자 API
            "/api/v1/admin/media/sync",
            "/api/v1/admin/recommendations",
            "/api/v1/admin/recommendations/cleanup",
            "/api/v1/admin/recommendations/expiry",
            "/api/v1/admin/recommendations/refresh",
            "/api/v1/admin/recommendations/stats",
            "/api/v1/admin/recommendations/threshold",
            
            // 애니메이션 API
            "/api/v1/animations/**",
            
            // 영화 API
            "/api/v1/movies/**",
            
            // TV 시리즈 API
            "/api/v1/tv/**",

            // OTT 인기 콘텐츠 API
            "/api/ott/**",

            // 게시판 API
            "/boards", "/boards/**",
            
            // 즐겨찾기 API
            "/bookmark/{boardId}", "/bookmark/{boardId}/count",
            
            // 댓글 API
            "/comment/{boardId}", "/comment/{boardId}/**",
            
            // 좋아요/싫어요 API
            "/like/{boardId}", "/like/{boardId}/count",
            "/dislike/{boardId}", "/dislike/{boardId}/count",
            
            // 신고 API
            "/report/{boardId}",
            
            // 감상평 API
            "/reply", "/reply/**",
            "/reply/bookmark/count", "/reply/bookmark/toggle",
            "/reply/judge/count/dislike", "/reply/judge/count/like",
            "/reply/judge/dislike", "/reply/judge/like",
            
            // 인증 API
            "/api/auth/kakao", "/api/auth/kakao/**",
            "/api/auth/google", "/api/auth/google/**",
            "/api/auth/naver", "/api/auth/naver/**",
            "/login", "/register",
            "/checkemail/{memEmail}", "/checknickname/{memNickname}",
            
            // Swagger/API 문서
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'"))
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> contentType.disable())
                )
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(PUBLIC_ENDPOINTS.toArray(new String[0])).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                );

        http.addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder bCrpytPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter();
    }
    
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
    
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration());
        return source;
    }

    private CorsConfiguration corsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:8081",
                "https://cinehive.com"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        return config;
    }
}

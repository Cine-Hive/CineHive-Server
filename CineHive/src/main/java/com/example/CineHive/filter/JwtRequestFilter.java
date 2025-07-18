package com.example.CineHive.filter;

import com.example.CineHive.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 HTTP 요청을 가로채 JWT(Json Web Token)의 유효성을 검증하는 필터입니다.
 * 토큰이 유효한 경우, Spring Security의 SecurityContext에 사용자 인증 정보를 설정합니다.
 * 이 필터는 Spring Security 설정에서 UsernamePasswordAuthenticationFilter 앞에 위치해야 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 실제 필터링 로직을 수행하는 메서드입니다.
     *
     * @param request      HTTP 요청
     * @param response     HTTP 응답
     * @param filterChain  필터 체인
     * @throws ServletException 서블릿 관련 예외
     * @throws IOException      입출력 관련 예외
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorizationHeader.substring(7);
        String email = null;

        try {
            email = jwtUtil.extractUsername(jwt);
        } catch (JwtException e) {
            // 토큰 파싱/만료 등 예외 발생 시 경고 로그만 남기고 통과시킵니다.
            // 인증 정보가 설정되지 않았으므로, 이후 Security Filter에서 접근이 거부됩니다.
            log.warn("JWT 토큰이 유효하지 않습니다. 원인: {}", e.getMessage());
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("Security Context에 사용자 인증 정보 설정 완료: {}", email);
                }
            } catch (UsernameNotFoundException e) {
                log.warn("JWT에 명시된 사용자를 DB에서 찾을 수 없습니다: {}", email);
            } catch (JwtException e) {
                // validateToken에서 발생할 수 있는 예외 처리
                log.warn("JWT 토큰 유효성 검증 실패: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}

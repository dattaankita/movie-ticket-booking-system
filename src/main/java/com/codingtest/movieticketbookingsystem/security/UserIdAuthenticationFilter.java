package com.codingtest.movieticketbookingsystem.security;

import com.codingtest.movieticketbookingsystem.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Simplified auth for the take-home scope: callers pass the seeded user id in X-User-Id.
 * Spring Security still enforces role-based access on admin routes.
 */
@Component
@RequiredArgsConstructor
public class UserIdAuthenticationFilter extends OncePerRequestFilter {

    public static final String USER_ID_HEADER = "X-User-Id";

    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (userIdHeader != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            parseAndAuthenticate(userIdHeader);
        }

        filterChain.doFilter(request, response);
    }

    private void parseAndAuthenticate(String userIdHeader) {
        try {
            Long userId = Long.parseLong(userIdHeader.trim());
            userRepository.findById(userId).ifPresent(user -> {
                UserPrincipal principal = new UserPrincipal(user);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        } catch (NumberFormatException ignored) {
            // Invalid header — downstream security returns 401
        }
    }
}

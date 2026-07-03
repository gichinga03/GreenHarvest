package com.greenharvest.auth.security;

import com.greenharvest.auth.service.JwtService;
import com.greenharvest.auth.service.TokenBlacklistService;
import com.greenharvest.user.model.User;
import com.greenharvest.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Runs before Spring's UsernamePasswordAuthenticationFilter on every
 * request. Three checks, in order, and it fails closed on any of them:
 *
 *   1. Is the JWT itself well-formed, signed correctly, and unexpired?
 *   2. Has this specific token (by jti) been blacklisted via /logout?
 *   3. Does the tokenVersion embedded in the token still match the
 *      current tokenVersion on the User row? (Handles Challenge D — an
 *      admin changing a user's role invalidates that user's outstanding
 *      tokens immediately, rather than waiting up to 15 minutes for
 *      natural expiry.)
 *
 * If all three pass, the request's SecurityContext is populated with an
 * Authentication built directly from the token's claims — no DB round
 * trip is needed for the role, only for the tokenVersion check.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtService.isTokenWellFormed(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jti = jwtService.extractJti(token);
        if (tokenBlacklistService.isBlacklisted(jti)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Already authenticated earlier in the chain — nothing to do.
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);
        Optional<User> maybeUser = userRepository.findByEmail(email);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            int tokenVersion = jwtService.extractTokenVersion(token);

            boolean stillValid = user.isActive() && tokenVersion == user.getTokenVersion();

            if (stillValid) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                var authToken = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}

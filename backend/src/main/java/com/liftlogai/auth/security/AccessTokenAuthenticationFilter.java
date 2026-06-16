package com.liftlogai.auth.security;

import com.liftlogai.auth.service.CookieService;
import com.liftlogai.auth.service.TokenPayload;
import com.liftlogai.auth.service.TokenService;
import com.liftlogai.user.entity.User;
import com.liftlogai.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AccessTokenAuthenticationFilter extends OncePerRequestFilter {

    private final CookieService cookieService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    public AccessTokenAuthenticationFilter(
            CookieService cookieService,
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.cookieService = cookieService;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            cookieService.readAccessToken(request)
                    .flatMap(token -> tokenService.validate(token, "access"))
                    .flatMap(this::findUser)
                    .ifPresent(user -> SecurityContextHolder.getContext().setAuthentication(authentication(user)));
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<User> findUser(TokenPayload payload) {
        return userRepository.findById(payload.userId());
    }

    private UsernamePasswordAuthenticationToken authentication(User user) {
        AuthenticatedUser principal = new AuthenticatedUser(user.getId(), user.getEmail());
        return new UsernamePasswordAuthenticationToken(principal, null, List.of());
    }
}

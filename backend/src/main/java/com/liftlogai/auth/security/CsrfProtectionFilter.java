package com.liftlogai.auth.security;

import com.liftlogai.auth.service.CookieService;
import com.liftlogai.auth.service.CsrfTokenService;
import com.liftlogai.common.error.ApiErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CsrfProtectionFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    private final CookieService cookieService;
    private final CsrfTokenService csrfTokenService;
    private final ApiErrorWriter errorWriter;

    public CsrfProtectionFilter(
            CookieService cookieService,
            CsrfTokenService csrfTokenService,
            ApiErrorWriter errorWriter
    ) {
        this.cookieService = cookieService;
        this.csrfTokenService = csrfTokenService;
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (requiresCsrf(request) && !hasValidCsrfToken(request)) {
            errorWriter.write(
                    response,
                    HttpStatus.FORBIDDEN.value(),
                    "CSRF_INVALID",
                    "A valid CSRF token is required.",
                    request.getRequestURI()
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean requiresCsrf(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod())
                && !EXCLUDED_PATHS.contains(request.getRequestURI())
                && SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private boolean hasValidCsrfToken(HttpServletRequest request) {
        String headerToken = request.getHeader(cookieService.csrfHeaderName());
        return headerToken != null
                && cookieService.readCsrfToken(request)
                .filter(cookieToken -> cookieToken.equals(headerToken))
                .filter(csrfTokenService::isValid)
                .isPresent();
    }
}

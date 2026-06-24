package cl.gradeops.ai.api.shared.infrastructure.config.security;

import cl.gradeops.ai.api.auth.application.port.out.AuthPort;
import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    private final AuthPort authPort;

    public FirebaseTokenFilter(AuthPort authPort) {
        this.authPort = authPort;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            TeacherIdentity identity = authPort.verifyToken(token);
            request.setAttribute("teacherIdentity", identity);

            AuthenticatedTeacher principal = new AuthenticatedTeacher(identity.uid(), identity.email());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Token verified successfully for uid={}", identity.uid());
        } catch (Exception e) {
            log.debug("Token verification failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

package cl.gradeops.ai.api.security;

import cl.gradeops.ai.api.port.TeacherIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class EmailVerifiedFilter extends OncePerRequestFilter {

    private static final List<String> WHITELIST = List.of(
            "/auth/register",
            "/auth/verify/resend"
    );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        TeacherIdentity identity = (TeacherIdentity) request.getAttribute("teacherIdentity");

        if (identity == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isWhitelisted(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!identity.emailVerified()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    OBJECT_MAPPER.writeValueAsString(Map.of("error", "EMAIL_NOT_VERIFIED")));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }
}

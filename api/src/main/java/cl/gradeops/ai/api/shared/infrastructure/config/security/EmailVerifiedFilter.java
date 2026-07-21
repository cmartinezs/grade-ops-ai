package cl.gradeops.ai.api.shared.infrastructure.config.security;

import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EmailVerifiedFilter extends OncePerRequestFilter {

    // Full servlet paths, matching AuthController's actual @RequestMapping("/api/v1/auth") +
    // @PostMapping mappings — HttpServletRequest.getRequestURI() returns the full path
    // including the /api/v1 prefix, not the path relative to the controller's own mapping.
    // A fresh self-registration's token always has emailVerified=false (chicken-and-egg:
    // registration is what lets a not-yet-verified user create their Teacher row at all),
    // so this whitelist must match the real path or every email/password registration 401s.
    private static final List<String> WHITELIST = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/verify/resend"
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

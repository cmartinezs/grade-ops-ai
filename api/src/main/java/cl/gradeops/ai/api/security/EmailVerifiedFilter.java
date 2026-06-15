package cl.gradeops.ai.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseToken;
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

/**
 * Rejects authenticated requests whose Firebase ID token has {@code email_verified = false}.
 *
 * <p>This filter must run <em>after</em> {@code FirebaseTokenFilter} in the filter chain.
 * {@code FirebaseTokenFilter} is responsible for verifying the raw Bearer token and storing the
 * decoded {@link FirebaseToken} as a request attribute named {@code "firebaseToken"} via:
 * <pre>request.setAttribute("firebaseToken", decodedToken);</pre>
 *
 * <p>Whitelisted paths (always allowed regardless of email verification status):
 * <ul>
 *   <li>{@code /auth/register}</li>
 *   <li>{@code /auth/verify/resend}</li>
 * </ul>
 */
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

        FirebaseToken decodedToken = (FirebaseToken) request.getAttribute("firebaseToken");

        // No token attribute → unauthenticated request; not this filter's concern.
        if (decodedToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Whitelisted paths always proceed.
        if (isWhitelisted(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Reject requests from users who have not verified their email.
        if (!decodedToken.isEmailVerified()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    OBJECT_MAPPER.writeValueAsString(Map.of("error", "EMAIL_NOT_VERIFIED"))
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }
}

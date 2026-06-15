package cl.gradeops.ai.api.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
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
            FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token, true);
            String uid = firebaseToken.getUid();
            String email = firebaseToken.getEmail();

            // Store the decoded token as a request attribute so downstream filters
            // (e.g. EmailVerifiedFilter) can read claims without re-verifying the token.
            request.setAttribute("firebaseToken", firebaseToken);

            AuthenticatedTeacher principal = new AuthenticatedTeacher(uid, email);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.debug("Firebase token verification failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}

package cl.gradeops.ai.api.shared.infrastructure.config.security;

import cl.gradeops.ai.api.auth.domain.model.TeacherIdentity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmailVerifiedFilterTest {

    private EmailVerifiedFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        filter = new EmailVerifiedFilter();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        responseBody = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));
    }

    @Test
    void unverified_token_on_protected_endpoint_returns_401() throws Exception {
        TeacherIdentity identity = unverifiedIdentity();
        when(request.getAttribute("teacherIdentity")).thenReturn(identity);
        when(request.getRequestURI()).thenReturn("/workspace/assessments");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(responseBody.toString()).contains("EMAIL_NOT_VERIFIED");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void verified_token_on_protected_endpoint_proceeds() throws Exception {
        TeacherIdentity identity = verifiedIdentity();
        when(request.getAttribute("teacherIdentity")).thenReturn(identity);
        when(request.getRequestURI()).thenReturn("/workspace/assessments");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/register", "/auth/verify/resend"})
    void unverified_token_on_whitelisted_path_proceeds(String path) throws Exception {
        TeacherIdentity identity = unverifiedIdentity();
        when(request.getAttribute("teacherIdentity")).thenReturn(identity);
        when(request.getRequestURI()).thenReturn(path);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void no_token_attribute_filter_skips() throws Exception {
        when(request.getAttribute("teacherIdentity")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/workspace/assessments");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    private TeacherIdentity unverifiedIdentity() {
        return new TeacherIdentity("uid-1", "teacher@school.com", false, "Teacher", "EMAIL_PASSWORD");
    }

    private TeacherIdentity verifiedIdentity() {
        return new TeacherIdentity("uid-1", "teacher@school.com", true, "Teacher", "EMAIL_PASSWORD");
    }
}

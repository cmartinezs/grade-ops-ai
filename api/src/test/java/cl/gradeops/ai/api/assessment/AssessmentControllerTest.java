package cl.gradeops.ai.api.assessment;

import cl.gradeops.ai.api.config.FirebaseTestConfig;
import cl.gradeops.ai.api.security.AuthenticatedTeacher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class AssessmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired FirebaseAuth firebaseAuth;
    @MockitoBean AssessmentService assessmentService;

    @BeforeEach
    void setUp() {
        reset(firebaseAuth);
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String uid, String email) {
        AuthenticatedTeacher teacher = new AuthenticatedTeacher(uid, email);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(teacher, null, List.of());
        SecurityContext ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @Test
    void authenticated_teacher_with_no_assessments_returns_200_empty_array() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-teacher-1");
        when(mockToken.getEmail()).thenReturn("teacher@school.com");
        when(mockToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token", true)).thenReturn(mockToken);
        when(assessmentService.listForTeacher("uid-teacher-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/assessments")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void unauthenticated_request_returns_401() throws Exception {
        mockMvc.perform(get("/api/v1/assessments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticated_teacher_with_assessments_returns_200_with_correct_fields() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-teacher-2");
        when(mockToken.getEmail()).thenReturn("teacher2@school.com");
        when(mockToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-2", true)).thenReturn(mockToken);

        AssessmentSummaryDto a1 = new AssessmentSummaryDto(
                "assess-1", "Java Basics", AssessmentStatus.OPEN, 10, 3, null);
        AssessmentSummaryDto a2 = new AssessmentSummaryDto(
                "assess-2", "Data Structures", AssessmentStatus.GRADING, 25, 0,
                "https://reports.example.com/assess-2");

        when(assessmentService.listForTeacher("uid-teacher-2")).thenReturn(List.of(a1, a2));

        mockMvc.perform(get("/api/v1/assessments")
                        .header("Authorization", "Bearer valid-token-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("assess-1"))
                .andExpect(jsonPath("$[0].title").value("Java Basics"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].submissionCount").value(10))
                .andExpect(jsonPath("$[0].pendingApprovals").value(3))
                .andExpect(jsonPath("$[0].reportLink").doesNotExist())
                .andExpect(jsonPath("$[1].id").value("assess-2"))
                .andExpect(jsonPath("$[1].title").value("Data Structures"))
                .andExpect(jsonPath("$[1].status").value("GRADING"))
                .andExpect(jsonPath("$[1].submissionCount").value(25))
                .andExpect(jsonPath("$[1].pendingApprovals").value(0))
                .andExpect(jsonPath("$[1].reportLink").value("https://reports.example.com/assess-2"));
    }
}

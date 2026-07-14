package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.in.CreateAssessmentBriefUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.GenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
import cl.gradeops.ai.api.assessment.application.port.in.RegenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import cl.gradeops.ai.api.shared.infrastructure.config.FirebaseTestConfig;
import cl.gradeops.ai.api.shared.infrastructure.config.security.AuthenticatedTeacher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
@ExtendWith(MockitoExtension.class)
class AssessmentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired FirebaseAuth firebaseAuth;
    @MockitoBean ListAssessmentsUseCase listAssessmentsUseCase;
    @MockitoBean CreateAssessmentBriefUseCase createAssessmentBriefUseCase;
    @MockitoBean GenerateAssessmentDraftUseCase generateAssessmentDraftUseCase;
    @MockitoBean RegenerateAssessmentDraftUseCase regenerateAssessmentDraftUseCase;
    @Mock FirebaseToken firebaseToken;

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
        when(firebaseToken.getUid()).thenReturn("uid-teacher-1");
        when(firebaseToken.getEmail()).thenReturn("teacher@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token", true)).thenReturn(firebaseToken);
        when(listAssessmentsUseCase.execute("uid-teacher-1")).thenReturn(List.of());

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
        when(firebaseToken.getUid()).thenReturn("uid-teacher-2");
        when(firebaseToken.getEmail()).thenReturn("teacher2@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-2", true)).thenReturn(firebaseToken);

        AssessmentSummaryResult r1 = AssessmentSummaryResult.builder()
                .id("assess-1").title("Java Basics").status(AssessmentStatus.OPEN)
                .submissionCount(10).pendingApprovals(3).reportLink(null).build();
        AssessmentSummaryResult r2 = AssessmentSummaryResult.builder()
                .id("assess-2").title("Data Structures").status(AssessmentStatus.GRADING)
                .submissionCount(25).pendingApprovals(0)
                .reportLink("https://reports.example.com/assess-2").build();
        when(listAssessmentsUseCase.execute("uid-teacher-2")).thenReturn(List.of(r1, r2));

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

    @Test
    void authenticated_teacher_posting_valid_brief_returns_201_with_assessmentId() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-3");
        when(firebaseToken.getEmail()).thenReturn("teacher3@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-3", true)).thenReturn(firebaseToken);
        when(createAssessmentBriefUseCase.execute(new CreateAssessmentBriefCommand(
                "uid-teacher-3", "Evaluate loops", "Java loops", "basic", "90min", "Java")))
                .thenReturn(new CreateAssessmentBriefResult("assess-new-1"));

        mockMvc.perform(post("/api/v1/assessments")
                        .header("Authorization", "Bearer valid-token-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "learningGoal": "Evaluate loops",
                                  "topic": "Java loops",
                                  "level": "basic",
                                  "duration": "90min",
                                  "language": "Java"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assessmentId").value("assess-new-1"));
    }

    @Test
    void posting_brief_with_blank_field_returns_422_and_does_not_invoke_use_case() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-4");
        when(firebaseToken.getEmail()).thenReturn("teacher4@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-4", true)).thenReturn(firebaseToken);

        mockMvc.perform(post("/api/v1/assessments")
                        .header("Authorization", "Bearer valid-token-4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "learningGoal": "",
                                  "topic": "Java loops",
                                  "level": "basic",
                                  "duration": "90min",
                                  "language": "Java"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(createAssessmentBriefUseCase);
    }

    @Test
    void unauthenticated_post_request_returns_401() throws Exception {
        mockMvc.perform(post("/api/v1/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "learningGoal": "Evaluate loops",
                                  "topic": "Java loops",
                                  "level": "basic",
                                  "duration": "90min",
                                  "language": "Java"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(createAssessmentBriefUseCase);
    }

    @Test
    void authenticated_teacher_posting_draft_generation_returns_201_with_draft() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-5");
        when(firebaseToken.getEmail()).thenReturn("teacher5@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-5", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();
        java.util.UUID draftId = java.util.UUID.randomUUID();
        when(generateAssessmentDraftUseCase.execute(new GenerateAssessmentDraftCommand(assessmentId, "uid-teacher-5")))
                .thenReturn(new GenerateAssessmentDraftResult(draftId, "Title", "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con"), 1));

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft")
                        .header("Authorization", "Bearer valid-token-5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.draftId").value(draftId.toString()))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.versionNumber").value(1))
                .andExpect(jsonPath("$.objectives[0]").value("obj"));
    }

    @Test
    void posting_draft_generation_for_unknown_assessment_returns_404() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-6");
        when(firebaseToken.getEmail()).thenReturn("teacher6@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-6", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();
        when(generateAssessmentDraftUseCase.execute(new GenerateAssessmentDraftCommand(assessmentId, "uid-teacher-6")))
                .thenThrow(new ResourceNotFoundException(assessmentId.toString()));

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft")
                        .header("Authorization", "Bearer valid-token-6"))
                .andExpect(status().isNotFound());
    }

    @Test
    void posting_draft_generation_when_agent_rejects_command_returns_422() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-7");
        when(firebaseToken.getEmail()).thenReturn("teacher7@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-7", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();
        when(generateAssessmentDraftUseCase.execute(new GenerateAssessmentDraftCommand(assessmentId, "uid-teacher-7")))
                .thenThrow(new AgentClientException(AgentClientException.Reason.AGENT_REJECTED, "rejected", null));

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft")
                        .header("Authorization", "Bearer valid-token-7"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void posting_draft_generation_when_agents_unreachable_returns_503() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-8");
        when(firebaseToken.getEmail()).thenReturn("teacher8@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-8", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();
        when(generateAssessmentDraftUseCase.execute(new GenerateAssessmentDraftCommand(assessmentId, "uid-teacher-8")))
                .thenThrow(new AgentClientException(AgentClientException.Reason.UNREACHABLE, "unreachable", null));

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft")
                        .header("Authorization", "Bearer valid-token-8"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void unauthenticated_draft_generation_request_returns_401() throws Exception {
        mockMvc.perform(post("/api/v1/assessments/" + java.util.UUID.randomUUID() + "/draft"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(generateAssessmentDraftUseCase);
    }

    @Test
    void authenticated_teacher_posting_draft_regeneration_returns_201_with_new_version() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-9");
        when(firebaseToken.getEmail()).thenReturn("teacher9@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-9", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();
        java.util.UUID draftId = java.util.UUID.randomUUID();
        when(regenerateAssessmentDraftUseCase.execute(new RegenerateAssessmentDraftCommand(
                assessmentId, "uid-teacher-9", "make it harder")))
                .thenReturn(new GenerateAssessmentDraftResult(draftId, "Title v2", "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con"), 2));

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft/regenerate")
                        .header("Authorization", "Bearer valid-token-9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "adjustmentNotes": "make it harder" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.draftId").value(draftId.toString()))
                .andExpect(jsonPath("$.title").value("Title v2"))
                .andExpect(jsonPath("$.versionNumber").value(2));
    }

    @Test
    void posting_draft_regeneration_without_prior_draft_returns_422() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-10");
        when(firebaseToken.getEmail()).thenReturn("teacher10@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-10", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();
        when(regenerateAssessmentDraftUseCase.execute(new RegenerateAssessmentDraftCommand(
                assessmentId, "uid-teacher-10", "make it harder")))
                .thenThrow(new NoPriorDraftException(assessmentId.toString()));

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft/regenerate")
                        .header("Authorization", "Bearer valid-token-10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "adjustmentNotes": "make it harder" }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void posting_draft_regeneration_with_blank_adjustment_notes_returns_422_and_does_not_invoke_use_case() throws Exception {
        when(firebaseToken.getUid()).thenReturn("uid-teacher-11");
        when(firebaseToken.getEmail()).thenReturn("teacher11@school.com");
        when(firebaseToken.isEmailVerified()).thenReturn(true);
        when(firebaseAuth.verifyIdToken("valid-token-11", true)).thenReturn(firebaseToken);
        java.util.UUID assessmentId = java.util.UUID.randomUUID();

        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/draft/regenerate")
                        .header("Authorization", "Bearer valid-token-11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "adjustmentNotes": "" }
                                """))
                .andExpect(status().isUnprocessableEntity());

        verifyNoInteractions(regenerateAssessmentDraftUseCase);
    }

    @Test
    void unauthenticated_draft_regeneration_request_returns_401() throws Exception {
        mockMvc.perform(post("/api/v1/assessments/" + java.util.UUID.randomUUID() + "/draft/regenerate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "adjustmentNotes": "make it harder" }
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(regenerateAssessmentDraftUseCase);
    }
}

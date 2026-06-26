package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web;

import cl.gradeops.ai.api.shared.domain.exception.DuplicateEmailException;
import cl.gradeops.ai.api.teacher.application.port.in.ProvisionTeacherUseCase;
import cl.gradeops.ai.api.teacher.application.port.in.UpdatePilotFlagsUseCase;
import cl.gradeops.ai.api.teacher.application.result.ProvisionTeacherResult;
import cl.gradeops.ai.api.teacher.application.result.UpdatePilotFlagsResult;
import cl.gradeops.ai.api.teacher.domain.exception.TeacherNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalTeacherController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "gradeops.web.base-url=http://localhost:3000/")
class InternalTeacherControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProvisionTeacherUseCase provisionTeacherUseCase;
    @MockitoBean UpdatePilotFlagsUseCase updatePilotFlagsUseCase;

    @Test
    void shouldReturn201WithInviteLinkWhenProvisioningSucceeds() throws Exception {
        when(provisionTeacherUseCase.execute(any()))
            .thenReturn(ProvisionTeacherResult.builder()
                .firebaseUid("uid-1").rawCode("code-abc").build());

        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Ana","lastName":"Soto","email":"a@x.com"}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firebaseUid").value("uid-1"))
            .andExpect(jsonPath("$.inviteLink").value(
                org.hamcrest.Matchers.containsString("/reset-password?code=code-abc")));
    }

    @Test
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        when(provisionTeacherUseCase.execute(any()))
            .thenThrow(new DuplicateEmailException("a@x.com"));

        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Ana","lastName":"Soto","email":"a@x.com"}
                        """))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200WithUpdatedFieldsWhenUpdatingPilotFlags() throws Exception {
        when(updatePilotFlagsUseCase.execute(any()))
            .thenReturn(UpdatePilotFlagsResult.builder()
                .firebaseUid("uid-1").planType("pilot").relatedParty(true).flagSetAt("2026-06-26T00:00:00Z")
                .build());

        mockMvc.perform(patch("/internal/teachers/uid-1/flags")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"planType":"pilot","relatedParty":true,"setBy":"admin"}
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.planType").value("pilot"))
            .andExpect(jsonPath("$.relatedParty").value(true));
    }

    @Test
    void shouldReturn404WhenTeacherNotFound() throws Exception {
        when(updatePilotFlagsUseCase.execute(any()))
            .thenThrow(new TeacherNotFoundException("uid-x"));

        mockMvc.perform(patch("/internal/teachers/uid-x/flags")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"planType":"pilot"}
                        """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn422WhenEmailIsMissing() throws Exception {
        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Ana","lastName":"Soto"}
                        """))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn422WhenFirstNameIsBlank() throws Exception {
        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"","lastName":"Soto","email":"a@x.com"}
                        """))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn422WhenEmailFormatIsInvalid() throws Exception {
        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Ana","lastName":"Soto","email":"not-an-email"}
                        """))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldReturn400WhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBuildWellFormedInviteLinkWhenBaseUrlHasTrailingSlash() throws Exception {
        when(provisionTeacherUseCase.execute(any()))
            .thenReturn(ProvisionTeacherResult.builder()
                .firebaseUid("uid-2").rawCode("code-xyz").build());

        mockMvc.perform(post("/internal/teachers")
                .header("X-Internal-Key", "test-internal-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName":"Ana","lastName":"Soto","email":"a@x.com"}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.inviteLink").value(
                org.hamcrest.Matchers.allOf(
                    org.hamcrest.Matchers.containsString("/reset-password?code=code-xyz"),
                    org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("//reset-password")))));
    }
}

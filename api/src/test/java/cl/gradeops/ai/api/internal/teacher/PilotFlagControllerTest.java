package cl.gradeops.ai.api.internal.teacher;

import cl.gradeops.ai.api.config.FirebaseTestConfig;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class PilotFlagControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired TeacherRepository teacherRepository;

    private static final String EXISTING_UID = "pilot-uid-001";

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
        TeacherEntity teacher = new TeacherEntity(EXISTING_UID, "Pilot Teacher", "pilot@example.com");
        teacherRepository.save(teacher);
    }

    @Test
    void valid_patch_updates_plan_type_and_related_party() throws Exception {
        mockMvc.perform(patch("/internal/teachers/" + EXISTING_UID + "/flags")
                        .header("X-Internal-Key", "test-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "planType": "pilot",
                                    "relatedParty": true,
                                    "offerDetails": "Free for early adopters",
                                    "evidenceLink": "https://evidence.gradeops.ai/deal-001",
                                    "setBy": "admin@gradeops.ai"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firebaseUid").value(EXISTING_UID))
                .andExpect(jsonPath("$.planType").value("pilot"))
                .andExpect(jsonPath("$.relatedParty").value(true))
                .andExpect(jsonPath("$.flagSetAt").isNotEmpty());

        TeacherEntity saved = teacherRepository.findById(EXISTING_UID).orElseThrow();
        assertThat(saved.getPlanType()).isEqualTo("pilot");
        assertThat(saved.isRelatedParty()).isTrue();
        assertThat(saved.getFlagSetAt()).isNotNull();
    }

    @Test
    void unknown_uid_returns_404() throws Exception {
        mockMvc.perform(patch("/internal/teachers/nonexistent-uid/flags")
                        .header("X-Internal-Key", "test-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planType": "pilot"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void missing_internal_key_returns_403() throws Exception {
        mockMvc.perform(patch("/internal/teachers/" + EXISTING_UID + "/flags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planType": "pilot"}
                                """))
                .andExpect(status().isForbidden());
    }
}

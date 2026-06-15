package cl.gradeops.ai.api.internal.teacher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import cl.gradeops.ai.api.config.FirebaseTestConfig;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class ProvisionTeacherControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired FirebaseAuth firebaseAuth;
    @Autowired TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
        reset(firebaseAuth);
    }

    @Test
    void validRequest_creates_firebase_user_and_teacher_record() throws Exception {
        UserRecord mockUser = mock(UserRecord.class);
        when(mockUser.getUid()).thenReturn("uid-123");
        when(firebaseAuth.createUser(any())).thenReturn(mockUser);
        when(firebaseAuth.generatePasswordResetLink("teacher@example.com")).thenReturn("https://reset.link");

        mockMvc.perform(post("/internal/teachers")
                        .header("X-Internal-Key", "test-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ada Lovelace", "email": "teacher@example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firebaseUid").value("uid-123"))
                .andExpect(jsonPath("$.inviteLink").value("https://reset.link"));

        assertThat(teacherRepository.existsByEmail("teacher@example.com")).isTrue();
    }

    @Test
    void duplicate_email_returns_409_with_no_partial_records() throws Exception {
        FirebaseAuthException dupEx = mock(FirebaseAuthException.class);
        com.google.firebase.auth.AuthErrorCode code = com.google.firebase.auth.AuthErrorCode.EMAIL_ALREADY_EXISTS;
        when(dupEx.getAuthErrorCode()).thenReturn(code);
        when(firebaseAuth.createUser(any())).thenThrow(dupEx);

        mockMvc.perform(post("/internal/teachers")
                        .header("X-Internal-Key", "test-internal-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ada Lovelace", "email": "dup@example.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EMAIL_ALREADY_EXISTS"));

        assertThat(teacherRepository.existsByEmail("dup@example.com")).isFalse();
    }

    @Test
    void missing_internal_key_returns_403() throws Exception {
        mockMvc.perform(post("/internal/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ada Lovelace", "email": "teacher@example.com"}
                                """))
                .andExpect(status().isForbidden());
    }
}

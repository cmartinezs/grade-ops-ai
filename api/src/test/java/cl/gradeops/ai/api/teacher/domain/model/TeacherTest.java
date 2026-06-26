package cl.gradeops.ai.api.teacher.domain.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

class TeacherTest {

    @Test
    void provision_sets_default_relatedParty_false() {
        Teacher t = Teacher.provision("uid-1", "Ana", "Soto", "a@x.com", AuthProvider.EMAIL_PASSWORD);
        assertThat(t.isRelatedParty()).isFalse();
    }

    @Test
    void provision_sets_createdAt_and_updatedAt() {
        Teacher t = Teacher.provision("uid-1", "Ana", "Soto", "a@x.com", AuthProvider.EMAIL_PASSWORD);
        assertThat(t.getCreatedAt()).isNotNull();
        assertThat(t.getUpdatedAt()).isNotNull();
    }

    @Test
    void restore_sets_all_fields_verbatim() {
        OffsetDateTime created = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updated = OffsetDateTime.now().minusHours(1);
        OffsetDateTime flagAt  = OffsetDateTime.now().minusMinutes(30);

        Teacher t = Teacher.restore("uid-2", "Grace", "Hopper", "g@x.com",
                AuthProvider.GOOGLE, created, updated,
                "pilot", true, "offer-details", "http://link", "admin", flagAt);

        assertThat(t.getFirebaseUid()).isEqualTo("uid-2");
        assertThat(t.getFirstName()).isEqualTo("Grace");
        assertThat(t.getLastName()).isEqualTo("Hopper");
        assertThat(t.getEmail()).isEqualTo("g@x.com");
        assertThat(t.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(t.getCreatedAt()).isEqualTo(created);
        assertThat(t.getUpdatedAt()).isEqualTo(updated);
        assertThat(t.getPlanType()).isEqualTo("pilot");
        assertThat(t.isRelatedParty()).isTrue();
        assertThat(t.getOfferDetails()).isEqualTo("offer-details");
        assertThat(t.getEvidenceLink()).isEqualTo("http://link");
        assertThat(t.getFlagSetBy()).isEqualTo("admin");
        assertThat(t.getFlagSetAt()).isEqualTo(flagAt);
    }

    @Test
    void updatePilotFlags_updates_non_null_fields_and_refreshes_timestamps() {
        Teacher t = Teacher.provision("uid-3", "Ada", "L", "a@x.com", AuthProvider.EMAIL_PASSWORD);

        t.updatePilotFlags("pilot", true, "offer", "http://ev", "admin");

        assertThat(t.getPlanType()).isEqualTo("pilot");
        assertThat(t.isRelatedParty()).isTrue();
        assertThat(t.getOfferDetails()).isEqualTo("offer");
        assertThat(t.getEvidenceLink()).isEqualTo("http://ev");
        assertThat(t.getFlagSetBy()).isEqualTo("admin");
        assertThat(t.getFlagSetAt()).isNotNull();
        assertThat(t.getUpdatedAt()).isNotNull();
    }

    @Test
    void updatePilotFlags_null_relatedParty_leaves_original_value() {
        Teacher t = Teacher.restore("uid-4", "X", "Y", "x@y.com",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, true, null, null, null, null);

        t.updatePilotFlags("pilot", null, null, null, null);

        assertThat(t.isRelatedParty()).isTrue();
    }

    @Test
    void teacherId_rejects_blank_value() {
        assertThatThrownBy(() -> new TeacherId(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void teacherId_rejects_null_value() {
        assertThatThrownBy(() -> new TeacherId(null))
                .isInstanceOf(NullPointerException.class);
    }
}

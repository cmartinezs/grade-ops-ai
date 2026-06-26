package cl.gradeops.ai.api.teacher.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.*;

class TeacherTest {

    @Test
    void shouldSetRelatedPartyToFalseWhenProvisioningNewTeacher() {
        Teacher t = Teacher.provision("uid-1", "Ana", "Soto", "a@x.com", AuthProvider.EMAIL_PASSWORD);
        assertThat(t.isRelatedParty()).isFalse();
    }

    @Test
    void shouldSetTimestampsWhenProvisioningNewTeacher() {
        Teacher t = Teacher.provision("uid-1", "Ana", "Soto", "a@x.com", AuthProvider.EMAIL_PASSWORD);
        assertThat(t.getCreatedAt()).isNotNull();
        assertThat(t.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
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
    void shouldUpdateNonNullFieldsAndRefreshTimestampsWhenUpdatingPilotFlags() {
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
    void shouldPreserveRelatedPartyWhenUpdatingWithNullValue() {
        Teacher t = Teacher.restore("uid-4", "X", "Y", "x@y.com",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, true, null, null, null, null);

        t.updatePilotFlags("pilot", null, null, null, null);

        assertThat(t.isRelatedParty()).isTrue();
    }

    @Test
    void shouldRejectBlankValueWhenCreatingTeacherId() {
        assertThatThrownBy(() -> new TeacherId(""))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectNullValueWhenCreatingTeacherId() {
        assertThatThrownBy(() -> new TeacherId(null))
                .isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectNullFirstNameWhenProvisioningTeacher() {
        assertThatThrownBy(() -> Teacher.provision("uid", null, "Soto", "a@x.com", AuthProvider.EMAIL_PASSWORD))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void shouldRejectBlankFirstNameWhenProvisioningTeacher() {
        assertThatThrownBy(() -> Teacher.provision("uid", "  ", "Soto", "a@x.com", AuthProvider.EMAIL_PASSWORD))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void shouldRejectNullLastNameWhenProvisioningTeacher() {
        assertThatThrownBy(() -> Teacher.provision("uid", "Ana", null, "a@x.com", AuthProvider.EMAIL_PASSWORD))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    void shouldRejectBlankEmailWhenProvisioningTeacher() {
        assertThatThrownBy(() -> Teacher.provision("uid", "Ana", "Soto", "", AuthProvider.EMAIL_PASSWORD))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("email");
    }

    @Test
    void shouldRejectNullAuthProviderWhenProvisioningTeacher() {
        assertThatThrownBy(() -> Teacher.provision("uid", "Ana", "Soto", "a@x.com", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("authProvider");
    }

    @Test
    void shouldRejectBlankFirstNameWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Teacher.restore("uid", "", "Soto", "a@x.com",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void shouldRejectNullLastNameWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Teacher.restore("uid", "Ana", null, "a@x.com",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    void shouldRejectBlankEmailWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Teacher.restore("uid", "Ana", "Soto", "  ",
                AuthProvider.EMAIL_PASSWORD, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("email");
    }

    @Test
    void shouldRejectNullAuthProviderWhenRestoringFromPersistence() {
        assertThatThrownBy(() -> Teacher.restore("uid", "Ana", "Soto", "a@x.com",
                null, OffsetDateTime.now(), OffsetDateTime.now(),
                null, false, null, null, null, null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("authProvider");
    }
}

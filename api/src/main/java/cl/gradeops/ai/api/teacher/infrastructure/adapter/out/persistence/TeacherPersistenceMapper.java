package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;

public class TeacherPersistenceMapper {

    Teacher toDomain(TeacherJpaEntity e) {
        return Teacher.restore(
            e.getFirebaseUid(), e.getFirstName(), e.getLastName(), e.getEmail(),
            AuthProvider.valueOf(e.getProvider()),
            e.getCreatedAt(), e.getUpdatedAt(),
            e.getPlanType(), e.isRelatedParty(),
            e.getOfferDetails(), e.getEvidenceLink(), e.getFlagSetBy(), e.getFlagSetAt()
        );
    }

    TeacherJpaEntity toJpa(Teacher t) {
        TeacherJpaEntity e = new TeacherJpaEntity();
        e.setFirebaseUid(t.getFirebaseUid());
        e.setFirstName(t.getFirstName());
        e.setLastName(t.getLastName());
        e.setEmail(t.getEmail());
        e.setProvider(t.getAuthProvider().name());
        e.setCreatedAt(t.getCreatedAt());
        e.setUpdatedAt(t.getUpdatedAt());
        e.setPlanType(t.getPlanType());
        e.setRelatedParty(t.isRelatedParty());
        e.setOfferDetails(t.getOfferDetails());
        e.setEvidenceLink(t.getEvidenceLink());
        e.setFlagSetBy(t.getFlagSetBy());
        e.setFlagSetAt(t.getFlagSetAt());
        return e;
    }
}

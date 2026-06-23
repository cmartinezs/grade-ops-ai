package cl.gradeops.ai.api.internal.teacher;

import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class PilotFlagService {

    private final TeacherRepository teacherRepository;

    public PilotFlagService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public PilotFlagResponse updateFlags(String uid, PilotFlagRequest req) {
        TeacherEntity teacher = teacherRepository.findById(uid)
                .orElseThrow(() -> new ResourceNotFoundException(uid));

        if (req.planType() != null) {
            teacher.setPlanType(req.planType());
        }
        if (req.relatedParty() != null) {
            teacher.setRelatedParty(req.relatedParty());
        }
        if (req.offerDetails() != null) {
            teacher.setOfferDetails(req.offerDetails());
        }
        if (req.evidenceLink() != null) {
            teacher.setEvidenceLink(req.evidenceLink());
        }
        if (req.setBy() != null) {
            teacher.setFlagSetBy(req.setBy());
        }

        OffsetDateTime now = OffsetDateTime.now();
        teacher.setFlagSetAt(now);
        teacher.setUpdatedAt(now);

        TeacherEntity saved = teacherRepository.save(teacher);

        return new PilotFlagResponse(
                saved.getFirebaseUid(),
                saved.getPlanType(),
                saved.isRelatedParty(),
                saved.getFlagSetAt() != null ? saved.getFlagSetAt().toString() : null
        );
    }
}

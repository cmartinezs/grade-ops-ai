package cl.gradeops.ai.api.domain.teacher;

import cl.gradeops.ai.api.config.FirebaseTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class TeacherRepositoryTest {

    @Autowired TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
    }

    @Test
    void findByPlanType_returns_only_matching_accounts() {
        TeacherEntity pilot1 = new TeacherEntity("uid-pilot-1", "Pilot One", "pilot1@example.com");
        pilot1.setPlanType("pilot");
        pilot1.setRelatedParty(true);

        TeacherEntity pilot2 = new TeacherEntity("uid-pilot-2", "Pilot Two", "pilot2@example.com");
        pilot2.setPlanType("pilot");

        TeacherEntity free1 = new TeacherEntity("uid-free-1", "Free One", "free1@example.com");
        free1.setPlanType("free");

        TeacherEntity noFlag = new TeacherEntity("uid-noflag-1", "No Flag", "noflag@example.com");

        teacherRepository.saveAll(List.of(pilot1, pilot2, free1, noFlag));

        List<TeacherEntity> pilots = teacherRepository.findByPlanType("pilot");
        assertThat(pilots).hasSize(2)
                .extracting(TeacherEntity::getFirebaseUid)
                .containsExactlyInAnyOrder("uid-pilot-1", "uid-pilot-2");

        List<TeacherEntity> freeAccounts = teacherRepository.findByPlanType("free");
        assertThat(freeAccounts).hasSize(1)
                .extracting(TeacherEntity::getFirebaseUid)
                .containsExactly("uid-free-1");
    }

    @Test
    void findByRelatedParty_returns_only_matching_accounts() {
        TeacherEntity related1 = new TeacherEntity("uid-rel-1", "Related One", "rel1@example.com");
        related1.setPlanType("pilot");
        related1.setRelatedParty(true);

        TeacherEntity related2 = new TeacherEntity("uid-rel-2", "Related Two", "rel2@example.com");
        related2.setPlanType("paid");
        related2.setRelatedParty(true);

        TeacherEntity notRelated = new TeacherEntity("uid-norel-1", "Not Related", "norel@example.com");
        notRelated.setPlanType("free");
        // relatedParty defaults to false

        teacherRepository.saveAll(List.of(related1, related2, notRelated));

        List<TeacherEntity> relatedParties = teacherRepository.findByRelatedParty(true);
        assertThat(relatedParties).hasSize(2)
                .extracting(TeacherEntity::getFirebaseUid)
                .containsExactlyInAnyOrder("uid-rel-1", "uid-rel-2");

        List<TeacherEntity> nonRelated = teacherRepository.findByRelatedParty(false);
        assertThat(nonRelated).hasSize(1)
                .extracting(TeacherEntity::getFirebaseUid)
                .containsExactly("uid-norel-1");
    }
}

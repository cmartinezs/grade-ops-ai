package cl.gradeops.ai.api.internal.teacher;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import cl.gradeops.ai.api.common.DuplicateEmailException;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProvisionTeacherService {

    private final FirebaseAuth firebaseAuth;
    private final TeacherRepository teacherRepository;

    public ProvisionTeacherService(FirebaseAuth firebaseAuth, TeacherRepository teacherRepository) {
        this.firebaseAuth = firebaseAuth;
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public ProvisionTeacherResponse provision(ProvisionTeacherRequest request) {
        String uid = null;
        try {
            String displayName = request.firstName() + " " + request.lastName();
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.email())
                    .setDisplayName(displayName)
                    .setEmailVerified(true);

            UserRecord userRecord = firebaseAuth.createUser(createRequest);
            uid = userRecord.getUid();

            TeacherEntity teacher = new TeacherEntity(uid, request.firstName(), request.lastName(), request.email());
            teacherRepository.save(teacher);

            String inviteLink = firebaseAuth.generatePasswordResetLink(request.email());
            return new ProvisionTeacherResponse(uid, inviteLink);

        } catch (FirebaseAuthException ex) {
            if ("EMAIL_ALREADY_EXISTS".equals(ex.getAuthErrorCode().name())) {
                throw new DuplicateEmailException(request.email());
            }
            throw new RuntimeException("Firebase user creation failed", ex);
        } catch (Exception ex) {
            // DB save failed — compensate by deleting the Firebase user
            if (uid != null) {
                try {
                    firebaseAuth.deleteUser(uid);
                } catch (FirebaseAuthException ignored) {}
            }
            throw new RuntimeException("Teacher provisioning failed", ex);
        }
    }
}

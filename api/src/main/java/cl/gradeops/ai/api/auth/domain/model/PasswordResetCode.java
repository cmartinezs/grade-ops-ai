package cl.gradeops.ai.api.auth.domain.model;

import cl.gradeops.ai.api.auth.domain.event.PasswordResetCodeIssuedEvent;
import cl.gradeops.ai.api.auth.domain.event.PasswordResetCodeUsedEvent;
import cl.gradeops.ai.api.auth.domain.valueobject.RawCode;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;
import lombok.Getter;

import java.time.Instant;

@Getter
public class PasswordResetCode extends AggregateRoot<String> {

    private final String teacherUid;
    private final RawCode rawCode;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant usedAt;

    private PasswordResetCode(String teacherUid, RawCode rawCode,
                              Instant expiresAt, Instant createdAt, Instant usedAt) {
        this.teacherUid = teacherUid;
        this.rawCode    = rawCode;
        this.expiresAt  = expiresAt;
        this.createdAt  = createdAt;
        this.usedAt     = usedAt;
    }

    @Override
    protected String id() {
        return teacherUid;
    }

    public static PasswordResetCode issue(String teacherUid, RawCode rawCode, Instant expiresAt) {
        PasswordResetCode code = new PasswordResetCode(teacherUid, rawCode, expiresAt, Instant.now(), null);
        code.registerEvent(new PasswordResetCodeIssuedEvent(teacherUid, expiresAt));
        return code;
    }

    public static PasswordResetCode restore(String teacherUid, RawCode rawCode,
                                            Instant expiresAt, Instant createdAt, Instant usedAt) {
        return new PasswordResetCode(teacherUid, rawCode, expiresAt, createdAt, usedAt);
    }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()    { return usedAt != null; }

    public void markUsed() {
        if (isUsed()) {
            throw new IllegalStateException("Password reset code already used");
        }
        this.usedAt = Instant.now();
        registerEvent(new PasswordResetCodeUsedEvent(teacherUid));
    }
}

package cl.gradeops.ai.api.auth.domain.model;

import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;
import java.time.Instant;

public class PasswordResetCode extends AggregateRoot<String> {

    private final String rawCode;
    private final Instant expiresAt;
    private final Instant createdAt;
    private Instant usedAt;

    private PasswordResetCode(String teacherUid, String rawCode,
                              Instant expiresAt, Instant createdAt, Instant usedAt) {
        super(teacherUid);
        this.rawCode   = rawCode;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.usedAt    = usedAt;
    }

    public static PasswordResetCode issue(String teacherUid, String rawCode, Instant expiresAt) {
        return new PasswordResetCode(teacherUid, rawCode, expiresAt, Instant.now(), null);
    }

    public static PasswordResetCode restore(String teacherUid, String rawCode,
                                            Instant expiresAt, Instant createdAt, Instant usedAt) {
        return new PasswordResetCode(teacherUid, rawCode, expiresAt, createdAt, usedAt);
    }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()    { return usedAt != null; }
    public void markUsed()     { this.usedAt = Instant.now(); }

    public String getTeacherUid() { return getId(); }
    public String getRawCode()    { return rawCode; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUsedAt()    { return usedAt; }
}

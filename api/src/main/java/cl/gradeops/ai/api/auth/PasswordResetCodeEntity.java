package cl.gradeops.ai.api.auth;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_codes")
public class PasswordResetCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "teacher_uid", nullable = false)
    private String teacherUid;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { this.createdAt = Instant.now(); }

    public PasswordResetCodeEntity(String teacherUid, String code, Instant expiresAt) {
        this.teacherUid = teacherUid;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    protected PasswordResetCodeEntity() {}

    public UUID getId()           { return id; }
    public String getTeacherUid() { return teacherUid; }
    public String getCode()       { return code; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt()    { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    public boolean isUsed()    { return usedAt != null; }
    public void markUsed()     { this.usedAt = Instant.now(); }
}

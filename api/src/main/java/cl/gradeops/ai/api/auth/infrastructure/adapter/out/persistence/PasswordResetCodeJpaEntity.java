package cl.gradeops.ai.api.auth.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_codes")
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetCodeJpaEntity {

    @Id
    @Column(name = "teacher_uid", nullable = false)
    private String teacherUid;

    @Column(name = "raw_code", nullable = false, unique = true)
    private String rawCode;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "used_at")
    private Instant usedAt;
}

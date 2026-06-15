package cl.gradeops.ai.api.domain.teacher;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "teacher")
public class TeacherEntity {

    @Id
    @Column(name = "firebase_uid", length = 128, nullable = false)
    private String firebaseUid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Pilot flag columns (V2 migration)
    @Column(name = "plan_type", length = 10)
    private String planType;

    @Column(name = "related_party", nullable = false)
    private boolean relatedParty = false;

    @Column(name = "offer_details", columnDefinition = "TEXT")
    private String offerDetails;

    @Column(name = "evidence_link", columnDefinition = "TEXT")
    private String evidenceLink;

    @Column(name = "flag_set_by", length = 255)
    private String flagSetBy;

    @Column(name = "flag_set_at")
    private OffsetDateTime flagSetAt;

    protected TeacherEntity() {}

    public TeacherEntity(String firebaseUid, String name, String email) {
        this(firebaseUid, name, email, "EMAIL_PASSWORD");
    }

    public TeacherEntity(String firebaseUid, String name, String email, String provider) {
        this.firebaseUid = firebaseUid;
        this.name = name;
        this.email = email;
        this.provider = provider;
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getFirebaseUid() { return firebaseUid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getProvider() { return provider; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public String getPlanType() { return planType; }
    public boolean isRelatedParty() { return relatedParty; }
    public String getOfferDetails() { return offerDetails; }
    public String getEvidenceLink() { return evidenceLink; }
    public String getFlagSetBy() { return flagSetBy; }
    public OffsetDateTime getFlagSetAt() { return flagSetAt; }

    public void setPlanType(String planType) { this.planType = planType; }
    public void setRelatedParty(boolean relatedParty) { this.relatedParty = relatedParty; }
    public void setOfferDetails(String offerDetails) { this.offerDetails = offerDetails; }
    public void setEvidenceLink(String evidenceLink) { this.evidenceLink = evidenceLink; }
    public void setFlagSetBy(String flagSetBy) { this.flagSetBy = flagSetBy; }
    public void setFlagSetAt(OffsetDateTime flagSetAt) { this.flagSetAt = flagSetAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}

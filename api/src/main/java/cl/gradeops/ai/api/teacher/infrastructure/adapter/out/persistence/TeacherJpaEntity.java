package cl.gradeops.ai.api.teacher.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "teacher")
class TeacherJpaEntity {

    @Id
    @Column(name = "firebase_uid", length = 128, nullable = false)
    private String firebaseUid;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "provider", length = 20, nullable = false)
    private String provider;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "plan_type", length = 10)
    private String planType;

    @Column(name = "related_party", nullable = false)
    private boolean relatedParty;

    @Column(name = "offer_details", columnDefinition = "TEXT")
    private String offerDetails;

    @Column(name = "evidence_link", columnDefinition = "TEXT")
    private String evidenceLink;

    @Column(name = "flag_set_by", length = 255)
    private String flagSetBy;

    @Column(name = "flag_set_at")
    private OffsetDateTime flagSetAt;

    protected TeacherJpaEntity() {}

    String getFirebaseUid()     { return firebaseUid; }
    String getFirstName()       { return firstName; }
    String getLastName()        { return lastName; }
    String getEmail()           { return email; }
    String getProvider()        { return provider; }
    OffsetDateTime getCreatedAt() { return createdAt; }
    OffsetDateTime getUpdatedAt() { return updatedAt; }
    String getPlanType()        { return planType; }
    boolean isRelatedParty()    { return relatedParty; }
    String getOfferDetails()    { return offerDetails; }
    String getEvidenceLink()    { return evidenceLink; }
    String getFlagSetBy()       { return flagSetBy; }
    OffsetDateTime getFlagSetAt() { return flagSetAt; }

    void setFirebaseUid(String v)     { this.firebaseUid = v; }
    void setFirstName(String v)       { this.firstName = v; }
    void setLastName(String v)        { this.lastName = v; }
    void setEmail(String v)           { this.email = v; }
    void setProvider(String v)        { this.provider = v; }
    void setCreatedAt(OffsetDateTime v) { this.createdAt = v; }
    void setUpdatedAt(OffsetDateTime v) { this.updatedAt = v; }
    void setPlanType(String v)        { this.planType = v; }
    void setRelatedParty(boolean v)   { this.relatedParty = v; }
    void setOfferDetails(String v)    { this.offerDetails = v; }
    void setEvidenceLink(String v)    { this.evidenceLink = v; }
    void setFlagSetBy(String v)       { this.flagSetBy = v; }
    void setFlagSetAt(OffsetDateTime v) { this.flagSetAt = v; }
}

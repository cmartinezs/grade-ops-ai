package cl.gradeops.ai.api.teacher.domain.model;

import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.OffsetDateTime;
import java.util.Objects;

public class Teacher extends AggregateRoot<TeacherId> {

    private TeacherId id;
    private String firstName;
    private String lastName;
    private String email;
    private AuthProvider authProvider;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String planType;
    private boolean relatedParty;
    private String offerDetails;
    private String evidenceLink;
    private String flagSetBy;
    private OffsetDateTime flagSetAt;

    private Teacher() {}

    public static Teacher provision(String firebaseUid, String firstName, String lastName,
                                    String email, AuthProvider authProvider) {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName must not be blank");
        if (lastName == null || lastName.isBlank())   throw new IllegalArgumentException("lastName must not be blank");
        if (email == null || email.isBlank())         throw new IllegalArgumentException("email must not be blank");
        Objects.requireNonNull(authProvider, "authProvider must not be null");
        Teacher t = new Teacher();
        t.id = new TeacherId(firebaseUid);
        t.firstName = firstName;
        t.lastName = lastName;
        t.email = email;
        t.authProvider = authProvider;
        OffsetDateTime now = OffsetDateTime.now();
        t.createdAt = now;
        t.updatedAt = now;
        t.relatedParty = false;
        return t;
    }

    public static Teacher restore(String firebaseUid, String firstName, String lastName,
                                  String email, AuthProvider authProvider,
                                  OffsetDateTime createdAt, OffsetDateTime updatedAt,
                                  String planType, boolean relatedParty, String offerDetails,
                                  String evidenceLink, String flagSetBy, OffsetDateTime flagSetAt) {
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("firstName must not be blank");
        if (lastName == null || lastName.isBlank())   throw new IllegalArgumentException("lastName must not be blank");
        if (email == null || email.isBlank())         throw new IllegalArgumentException("email must not be blank");
        Objects.requireNonNull(authProvider, "authProvider must not be null");
        Teacher t = new Teacher();
        t.id = new TeacherId(firebaseUid);
        t.firstName = firstName;
        t.lastName = lastName;
        t.email = email;
        t.authProvider = authProvider;
        t.createdAt = createdAt;
        t.updatedAt = updatedAt;
        t.planType = planType;
        t.relatedParty = relatedParty;
        t.offerDetails = offerDetails;
        t.evidenceLink = evidenceLink;
        t.flagSetBy = flagSetBy;
        t.flagSetAt = flagSetAt;
        return t;
    }

    public void updatePilotFlags(String planType, Boolean relatedParty, String offerDetails,
                                 String evidenceLink, String setBy) {
        if (planType != null)     this.planType = planType;
        if (relatedParty != null) this.relatedParty = relatedParty;
        if (offerDetails != null) this.offerDetails = offerDetails;
        if (evidenceLink != null) this.evidenceLink = evidenceLink;
        if (setBy != null)        this.flagSetBy = setBy;
        OffsetDateTime now = OffsetDateTime.now();
        this.flagSetAt = now;
        this.updatedAt = now;
    }

    @Override protected TeacherId id()            { return id; }
    public String getFirebaseUid()               { return id.value(); }
    public String getFirstName()                 { return firstName; }
    public String getLastName()                  { return lastName; }
    public String getEmail()                     { return email; }
    public AuthProvider getAuthProvider()        { return authProvider; }
    public OffsetDateTime getCreatedAt()         { return createdAt; }
    public OffsetDateTime getUpdatedAt()         { return updatedAt; }
    public String getPlanType()                  { return planType; }
    public boolean isRelatedParty()              { return relatedParty; }
    public String getOfferDetails()              { return offerDetails; }
    public String getEvidenceLink()              { return evidenceLink; }
    public String getFlagSetBy()                 { return flagSetBy; }
    public OffsetDateTime getFlagSetAt()         { return flagSetAt; }
}

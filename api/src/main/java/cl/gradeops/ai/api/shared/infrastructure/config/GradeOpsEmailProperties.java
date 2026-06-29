package cl.gradeops.ai.api.shared.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gradeops.email")
public class GradeOpsEmailProperties {

    private String fromAddress;
    private String fromName;
    private String supportEmail;
    private ResetPassword resetPassword = new ResetPassword();

    public static class ResetPassword {
        private int ttlMinutes = 30;
        private String subject = "Restablece tu contraseña en GradeOps AI";

        public int getTtlMinutes()       { return ttlMinutes; }
        public String getSubject()       { return subject; }
        public void setTtlMinutes(int v) { this.ttlMinutes = v; }
        public void setSubject(String v) { this.subject = v; }
    }

    public String getFromAddress()                { return fromAddress; }
    public String getFromName()                   { return fromName; }
    public String getSupportEmail()               { return supportEmail; }
    public ResetPassword getResetPassword()        { return resetPassword; }
    public void setFromAddress(String v)           { this.fromAddress = v; }
    public void setFromName(String v)              { this.fromName = v; }
    public void setSupportEmail(String v)          { this.supportEmail = v; }
    public void setResetPassword(ResetPassword v)  { this.resetPassword = v; }
}

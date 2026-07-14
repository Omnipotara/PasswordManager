package MFA;

import Config.AppConfig;
import Config.ConfigLoader;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Sends OTP codes through the configured SMTP account.
 */
public class EmailService {

    private static final String OTP_SUBJECT = "Vas verifikacioni kod";

    private final AppConfig config;

    public EmailService() {
        this(ConfigLoader.load());
    }

    public EmailService(AppConfig config) {
        this.config = config;
    }

    public void sendOtpCode(String recipientEmail, String otpCode) {
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required.");
        }
        if (otpCode == null || otpCode.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP code is required.");
        }

        try {
            Message message = new MimeMessage(createSession());
            message.setFrom(new InternetAddress(config.getSmtpUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail.trim()));
            message.setSubject(OTP_SUBJECT);
            message.setText("Vas verifikacioni kod je: " + otpCode);

            Transport.send(message);
        } catch (MessagingException ex) {
            throw new IllegalStateException("Unable to send OTP email.", ex);
        }
    }

    private Session createSession() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", config.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(config.getSmtpPort()));
        properties.put("mail.smtp.auth", String.valueOf(config.isSmtpAuth()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(config.isSmtpStartTls()));

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getSmtpUsername(), config.getSmtpPassword());
            }
        });
    }
}

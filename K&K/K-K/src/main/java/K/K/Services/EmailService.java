package K.K.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StreamUtils;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ResourceLoader resourceLoader;

    // Inject Spring's ResourceLoader automatically
    public EmailService(JavaMailSender mailSender, ResourceLoader resourceLoader) {
        this.mailSender = mailSender;
        this.resourceLoader = resourceLoader;
    }

    public void sendVerificationCode(String to, String subject, String sixDigitCode) {
        try {
            // 1. Read the template file from the classpath resources folder
            Resource resource = resourceLoader.getResource("classpath:/2fa-template.html");
            String htmlTemplate = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            // 2. Inject the dynamic code into the static HTML layout
            String finalizedHtml = htmlTemplate.replace("[VERIFICATION_CODE]", sixDigitCode);

            // 3. Build and dispatch the message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(finalizedHtml, true); // Keep true flag for rich text rendering

            mailSender.send(message);

        } catch (IOException | MessagingException e) {
            throw new IllegalStateException("Failed to parse or send verification email layout", e);
        }
    }
}

package auth.res_server.demo.service.impl;

import auth.res_server.demo.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@example.com}")
    private String fromEmail;

    @Value("${app.mail.base-url:http://localhost:9000}")
    private String baseUrl;

    @Override
    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        String subject = "Verify Your Email Address";
        String verificationUrl = baseUrl + "/api/v1/auth/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("expirationHours", 24);

        String htmlContent = templateEngine.process("email/verification", context);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        String subject = "Reset Your Password";
        String resetUrl = baseUrl + "/api/v1/auth/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expirationHours", 1);

        String htmlContent = templateEngine.process("email/password-reset", context);

        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}

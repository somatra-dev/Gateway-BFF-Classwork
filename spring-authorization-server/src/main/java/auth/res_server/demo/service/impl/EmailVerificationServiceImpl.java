package auth.res_server.demo.service.impl;

import auth.res_server.demo.domain.EmailVerificationToken;
import auth.res_server.demo.domain.User;
import auth.res_server.demo.repository.EmailVerificationTokenRepository;
import auth.res_server.demo.repository.UserRepository;
import auth.res_server.demo.service.EmailService;
import auth.res_server.demo.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private static final int MAX_RESEND_PER_HOUR = 3;
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void sendVerificationEmail(User user) {
        if (user.getEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already verified");
        }

        // Delete any existing tokens for this user
        tokenRepository.deleteAllByUser(user);

        // Generate new token
        String rawToken = generateSecureToken();
        String hashedToken = hashToken(rawToken);

        // Save hashed token
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .tokenHash(hashedToken)
                .user(user)
                .expiresAt(Instant.now().plus(TOKEN_EXPIRATION_HOURS, ChronoUnit.HOURS))
                .build();

        tokenRepository.save(verificationToken);

        // Send email with raw token
        emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), rawToken);

        log.info("Verification email sent to user: {}", user.getUsername());
    }

    @Override
    public void verifyEmail(String token) {
        String hashedToken = hashToken(token);

        EmailVerificationToken verificationToken = tokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        if (verificationToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification token has already been used");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        tokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getUsername());
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // Return generic message to prevent email enumeration
                    log.warn("Resend verification attempted for non-existent email");
                    return new ResponseStatusException(HttpStatus.OK, "If the email exists, a verification email has been sent");
                });

        if (user.getEmailVerified()) {
            // Don't reveal that email is already verified
            log.info("Resend verification attempted for already verified email: {}", email);
            return;
        }

        // Rate limiting: check tokens created in the last hour
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        long recentTokenCount = tokenRepository.countByUserAndCreatedAtAfter(user, oneHourAgo);

        if (recentTokenCount >= MAX_RESEND_PER_HOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many verification emails requested. Please try again later.");
        }

        sendVerificationEmail(user);
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

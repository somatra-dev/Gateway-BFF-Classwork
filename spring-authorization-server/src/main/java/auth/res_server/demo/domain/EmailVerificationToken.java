package auth.res_server.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    private static final int EXPIRATION_HOURS = 24;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean used = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        if (this.expiresAt == null) {
            this.expiresAt = Instant.now().plusSeconds(EXPIRATION_HOURS * 3600L);
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public static EmailVerificationTokenBuilder builder() {
        return new EmailVerificationTokenBuilder();
    }

    public static class EmailVerificationTokenBuilder {
        private final EmailVerificationToken token = new EmailVerificationToken();

        public EmailVerificationTokenBuilder tokenHash(String tokenHash) {
            token.tokenHash = tokenHash;
            return this;
        }

        public EmailVerificationTokenBuilder user(User user) {
            token.user = user;
            return this;
        }

        public EmailVerificationTokenBuilder expiresAt(Instant expiresAt) {
            token.expiresAt = expiresAt;
            return this;
        }

        public EmailVerificationToken build() {
            return token;
        }
    }
}

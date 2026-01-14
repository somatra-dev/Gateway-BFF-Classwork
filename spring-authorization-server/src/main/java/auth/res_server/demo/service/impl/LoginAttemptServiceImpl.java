package auth.res_server.demo.service.impl;

import auth.res_server.demo.domain.User;
import auth.res_server.demo.repository.UserRepository;
import auth.res_server.demo.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void loginSucceeded(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getFailedAttempts() > 0 || user.getLockTime() != null) {
                user.setFailedAttempts(0);
                user.setLockTime(null);
                user.setAccountNonLocked(true);
                userRepository.save(user);
                log.info("Login succeeded for user: {}. Failed attempts reset.", username);
            }
        }
    }

    @Override
    @Transactional
    public void loginFailed(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if lock has expired
            if (user.getLockTime() != null && isLockExpired(user.getLockTime())) {
                user.setFailedAttempts(0);
                user.setLockTime(null);
                user.setAccountNonLocked(true);
            }

            int newFailedAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(newFailedAttempts);

            if (newFailedAttempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountNonLocked(false);
                user.setLockTime(Instant.now());
                log.warn("User {} has been locked due to {} failed login attempts.", username, newFailedAttempts);
            } else {
                log.info("Failed login attempt {} of {} for user: {}", newFailedAttempts, MAX_FAILED_ATTEMPTS, username);
            }

            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public boolean isAccountLocked(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        if (!user.isAccountNonLocked() && user.getLockTime() != null) {
            if (isLockExpired(user.getLockTime())) {
                // Unlock the account
                user.setAccountNonLocked(true);
                user.setLockTime(null);
                user.setFailedAttempts(0);
                userRepository.save(user);
                log.info("Account lock expired for user: {}. Account unlocked.", username);
                return false;
            }
            return true;
        }

        return false;
    }

    @Override
    public int getRemainingAttempts(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return MAX_FAILED_ATTEMPTS;
        }
        return Math.max(0, MAX_FAILED_ATTEMPTS - userOpt.get().getFailedAttempts());
    }

    private boolean isLockExpired(Instant lockTime) {
        Duration duration = Duration.between(lockTime, Instant.now());
        return duration.toMinutes() >= LOCK_DURATION_MINUTES;
    }

    public long getRemainingLockTimeMinutes(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty() || userOpt.get().getLockTime() == null) {
            return 0;
        }

        Instant lockTime = userOpt.get().getLockTime();
        Duration elapsed = Duration.between(lockTime, Instant.now());
        long remainingMinutes = LOCK_DURATION_MINUTES - elapsed.toMinutes();
        return Math.max(0, remainingMinutes);
    }
}

package auth.res_server.demo.service;

public interface LoginAttemptService {

    int MAX_FAILED_ATTEMPTS = 5;
    long LOCK_DURATION_MINUTES = 15;

    void loginSucceeded(String username);

    void loginFailed(String username);

    boolean isAccountLocked(String username);

    int getRemainingAttempts(String username);
}

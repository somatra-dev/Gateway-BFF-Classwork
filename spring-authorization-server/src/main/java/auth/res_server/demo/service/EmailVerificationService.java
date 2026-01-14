package auth.res_server.demo.service;

import auth.res_server.demo.domain.User;

public interface EmailVerificationService {

    void sendVerificationEmail(User user);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}

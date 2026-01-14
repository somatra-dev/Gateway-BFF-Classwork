package auth.res_server.demo.service.impl;

import auth.res_server.demo.config.CustomUserDetails;
import auth.res_server.demo.domain.User;
import auth.res_server.demo.repository.UserRepository;
import auth.res_server.demo.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Check if account is locked before loading user
        if (loginAttemptService.isAccountLocked(username)) {
            throw new LockedException("Account is locked due to too many failed login attempts. Please try again later.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );

        if (!user.getEmailVerified()) {
            throw new DisabledException("Email not verified. Please verify your email before logging in.");
        }

        return new CustomUserDetails(user);
    }
}
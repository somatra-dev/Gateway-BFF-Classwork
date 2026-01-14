package auth.res_server.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "gender")
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    // OAuth2 fields
    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    // Account status flags
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "lock_time")
    private Instant lockTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    /* ===================== UserDetails implementation ===================== */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        // Add role names as authorities (e.g., "ROLE_USER", "ROLE_ADMIN")
        roles.forEach(role ->
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getName())));

        // Add fine-grained authorities from roles
        roles.stream()
                .flatMap(role -> role.getAuthorities().stream())
                .forEach(authority ->
                        grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName())));

        return grantedAuthorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /* ===================== Convenience getters for CustomUserDetails ===================== */

    public String getGivenName() {
        return firstName;
    }

    public String getFamilyName() {
        return lastName;
    }

    public String getProfileImage() {
        return profilePictureUrl;
    }

    public String getCoverImage() {
        return coverImageUrl;
    }

    public Boolean getIsEnabled() {
        return enabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    // Helper to check if it's an OAuth2-registered user
    public boolean isOAuth2User() {
        return provider != null && !"local".equals(provider);
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private final User user = new User();

        public UserBuilder uuid(String uuid) { user.uuid = uuid; return this; }
        public UserBuilder username(String username) { user.username = username; return this; }
        public UserBuilder password(String password) { user.password = password; return this; }
        public UserBuilder email(String email) { user.email = email; return this; }
        public UserBuilder emailVerified(boolean verified) { user.emailVerified = verified; return this; }
        public UserBuilder firstName(String firstName) { user.firstName = firstName; return this; }
        public UserBuilder lastName(String lastName) { user.lastName = lastName; return this; }
        public UserBuilder phoneNumber(String phoneNumber) { user.phoneNumber = phoneNumber; return this; }
        public UserBuilder gender(String gender) { user.gender = gender; return this; }
        public UserBuilder dob(LocalDate dob) { user.dob = dob; return this; }
        public UserBuilder profilePictureUrl(String url) { user.profilePictureUrl = url; return this; }
        public UserBuilder coverImageUrl(String url) { user.coverImageUrl = url; return this; }
        public UserBuilder provider(String provider) { user.provider = provider; return this; }
        public UserBuilder providerId(String providerId) { user.providerId = providerId; return this; }
        public UserBuilder roles(List<Role> roles) { user.roles = roles; return this; }
        public UserBuilder enabled(boolean enabled) { user.enabled = enabled; return this; }
        public UserBuilder failedAttempts(int failedAttempts) { user.failedAttempts = failedAttempts; return this; }
        public UserBuilder lockTime(Instant lockTime) { user.lockTime = lockTime; return this; }
        public UserBuilder accountNonLocked(boolean accountNonLocked) { user.accountNonLocked = accountNonLocked; return this; }
        public User build() { return user; }
    }
}
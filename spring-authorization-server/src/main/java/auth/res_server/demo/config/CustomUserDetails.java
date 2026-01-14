package auth.res_server.demo.config;

import auth.res_server.demo.domain.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Custom UserDetails implementation.
 * Replace the placeholder methods with real data from your database/entity.
 */
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String uuid;
    private final String username;
    private final String email;
    private final String password;
    private final String familyName;
    private final String givenName;
    private final String phoneNumber;
    private final String gender;
    private final LocalDate dob;
    private final String profileImage;
    private final String coverImage;
    private final Boolean accountNonExpired;
    private final Boolean accountNonLocked;
    private final Boolean credentialsNonExpired;
    private final Boolean enabled;
    private final Boolean emailVerified;
    private final Set<GrantedAuthority> authorities;

    // Constructor for creating from User entity (used in UserDetailsService)
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.uuid = user.getUuid();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.familyName = user.getFamilyName();
        this.givenName = user.getGivenName();
        this.phoneNumber = user.getPhoneNumber();
        this.gender = user.getGender();
        this.dob = user.getDob();
        this.profileImage = user.getProfileImage();
        this.coverImage = user.getCoverImage();
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.enabled = user.getIsEnabled();
        this.emailVerified = user.getEmailVerified();
        this.authorities = user.getAuthorities().stream()
                .map(userAuth -> new SimpleGrantedAuthority(userAuth.getAuthority()))
                .collect(Collectors.toSet());
    }


    // Jackson deserialization constructor (for Redis/Session deserialization)
    @JsonCreator
    public CustomUserDetails(
            @JsonProperty("id") Long id,
            @JsonProperty("uuid") String uuid,
            @JsonProperty("username") String username,
            @JsonProperty("email") String email,
            @JsonProperty("password") String password,
            @JsonProperty("familyName") String familyName,
            @JsonProperty("givenName") String givenName,
            @JsonProperty("phoneNumber") String phoneNumber,
            @JsonProperty("gender") String gender,
            @JsonProperty("dob") LocalDate dob,
            @JsonProperty("profileImage") String profileImage,
            @JsonProperty("coverImage") String coverImage,
            @JsonProperty("accountNonExpired") Boolean accountNonExpired,
            @JsonProperty("accountNonLocked") Boolean accountNonLocked,
            @JsonProperty("credentialsNonExpired") Boolean credentialsNonExpired,
            @JsonProperty("enabled") Boolean enabled,
            @JsonProperty("emailVerified") Boolean emailVerified,
            @JsonProperty("authorities") Set<GrantedAuthority> authorities) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.password = password;
        this.familyName = familyName;
        this.givenName = givenName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.dob = dob;
        this.profileImage = profileImage;
        this.coverImage = coverImage;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.authorities = authorities;
    }

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired != null && accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked != null && accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired != null && credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    // Convenience methods for full name
    public String getFullName() {
        return givenName + " " + familyName;
    }
}
package auth.res_server.demo.security;

import auth.res_server.demo.config.CustomUserDetails;
import auth.res_server.demo.service.impl.CustomUserDetailsService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final PasswordEncoder passwordEncoder;


    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.apply(authorizationServerConfigurer);


        authorizationServerConfigurer
                .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage("/oauth2/consent"))
                .oidc(oidc -> oidc
                        // Enable OIDC configuration endpoint
                        .providerConfigurationEndpoint(providerConfigurationEndpoint -> {

                        })
                );
        http
                .securityMatcher(
                        "/oauth2/**",
                        "/.well-known/**",
                        "/connect/**",
                        "/userinfo"
                )
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers(
                                "/.well-known/openid-configuration",
                                "/.well-known/oauth-authorization-server",
                                "/oauth2/jwks"
                        ).permitAll()
                        .requestMatchers("/connect/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/.well-known/openid-configuration",
                        "/.well-known/oauth-authorization-server",
                        "/oauth2/jwks"
                ))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint("/login")
                        )
                );

        return http.build();
    }

//    @Bean
//    @Order(1)
//    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
//
//        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
//        http.apply(authorizationServerConfigurer);
//
//        http
//                .oauth2AuthorizationServer(authorizationServer -> {
//                    // This automatically installs ALL needed filters + correct securityMatcher
//                    http.securityMatcher(authorizationServer.getEndpointsMatcher());
//
//                    authorizationServer
//                            .authorizationEndpoint(a ->
//                                    a.consentPage("/oauth2/consent")
//                            );

    /// /
//                })
//
//
//                // OAuth2 endpoints require authentication (normal)
//                .authorizeHttpRequests(authorize ->
//                        authorize
//                                .requestMatchers("/oauth2/token",
//                                        "/oauth2/jwks",
//                                        "/oauth2/introspect",
//                                        "/oauth2/revoke",
//                                        "/connect/logout",
//                                        "/.well-known/openid-configuration",
//                                        "/.well-known/oauth-authorization-server")
//                                .permitAll()
//                                .anyRequest().authenticated()
//                )
//                .csrf(AbstractHttpConfigurer::disable)
//
//                // Redirect to /login when the user is not authenticated
//                // and the request is HTML (browser)
//                .exceptionHandling(exceptions -> exceptions
//                        .defaultAuthenticationEntryPointFor(
//                                new LoginUrlAuthenticationEntryPoint("/login"),
//                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
//                        )
//                );
//
//        return http.build();
//    }
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // CSRF token handler for SPA clients
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null); // Required for SPA to work with deferred tokens

        http
                .securityMatcher("/**")  // Match all other requests
                .authorizeHttpRequests(authorize ->
                        authorize
                                // Public endpoints
                                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                                .requestMatchers("/api/v1/auth/verify-email").permitAll()
                                .requestMatchers("/api/v1/auth/resend-verification").permitAll()
                                .requestMatchers("/login", "/register", "/error", "/css/**", "/js/**").permitAll()
                                // Admin endpoints - require ROLE_ADMIN
                                .requestMatchers("/api/v1/clients/**").permitAll()
                                .requestMatchers("/api/v1/roles/**").permitAll()
                                .requestMatchers("/api/v1/authorities/**").permitAll()
                                // All other requests require authentication
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        // Use cookie-based CSRF token repository for SPA clients
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                        // Ignore CSRF for stateless API endpoints (use Bearer token auth)
                        .ignoringRequestMatchers(
                                "/api/v1/**",
                                "/oauth2/token",
                                "/oauth2/introspect",
                                "/oauth2/revoke"
                        )
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }


    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .authorizationEndpoint("/oauth2/authorize")
                .tokenEndpoint("/oauth2/token")
                .jwkSetEndpoint("/oauth2/jwks")
                .tokenRevocationEndpoint("/oauth2/revoke")
                .tokenIntrospectionEndpoint("/oauth2/introspect")
                .oidcUserInfoEndpoint("/userinfo")
                .oidcClientRegistrationEndpoint("/connect/register")
                .oidcLogoutEndpoint("/connect/logout")
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            Authentication authentication = context.getPrincipal();
            if (authentication.getPrincipal() instanceof CustomUserDetails user) {

                JwtClaimsSet.Builder claims = context.getClaims();

                // Required: sub must never be null → fallback to username
                claims.claim("sub", nonNullOr(user.getUuid(), user.getUsername()));

                if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                    String sessionId = context.getAuthorization().getId();
                    claims.claim("sid", sessionId);
                }

                // Safe helper: only add claim if value is not null
                claimIfNotNull(claims, "email", user.getEmail());
                claimIfNotNull(claims, "email_verified", user.getEmailVerified());
                claimIfNotNull(claims, "name", user.getFullName());
                claimIfNotNull(claims, "given_name", user.getGivenName());
                claimIfNotNull(claims, "family_name", user.getFamilyName());
                claimIfNotNull(claims, "phone_number", user.getPhoneNumber());
                claimIfNotNull(claims, "gender", user.getGender());
                claimIfNotNull(claims, "picture", user.getProfileImage());
                claimIfNotNull(claims, "uuid", user.getUuid());

                // birthdate → format as String (ISO date), skip if null
                if (user.getDob() != null) {
                    claims.claim("birthdate", user.getDob().toString());
                }

                // Separate roles and permissions
                Set<String> allAuthorities = user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

                // Roles: authorities starting with "ROLE_"
                Set<String> roles = allAuthorities.stream()
                        .filter(auth -> auth.startsWith("ROLE_"))
                        .collect(Collectors.toSet());
                claims.claim("roles", roles);

                // Authorities: all granted authorities (roles + permissions combined)
//                claims.claim("authorities", allAuthorities);

                // Permissions: fine-grained authorities (not starting with "ROLE_")
                Set<String> permissions = allAuthorities.stream()
                        .filter(auth -> !auth.startsWith("ROLE_"))
                        .collect(Collectors.toSet());
                if (!permissions.isEmpty()) {
                    claims.claim("permissions", permissions);
                }

                // Scope (required for access token)
                if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                    String scope = context.getAuthorizedScopes() != null
                            ? String.join(" ", context.getAuthorizedScopes())
                            : String.join(" ", allAuthorities);
                    claims.claim("scope", scope);
                }
            }
        };
    }

    // Helper methods — put them in the same class or as static utilities
    private static String nonNullOr(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private static void claimIfNotNull(JwtClaimsSet.Builder claims, String name, Object value) {
        if (value != null) {
            claims.claim(name, value);
        }
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("http://127.0.0.1:9000");
        config.addAllowedOrigin("http://localhost:9000");
        config.addAllowedOrigin("http://localhost:8888");  // API Gateway
        config.addAllowedOrigin("http://127.0.0.1:8888");
        config.addAllowedOrigin("http://localhost:3000");  // Frontend
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}

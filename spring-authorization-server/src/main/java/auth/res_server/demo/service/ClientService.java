package auth.res_server.demo.service;

import auth.res_server.demo.domain.Client;
import auth.res_server.demo.dto.client.*;
import auth.res_server.demo.repository.ClientRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public ClientService(ClientRepository clientRepository,
                         PasswordEncoder passwordEncoder,
                         ObjectMapper objectMapper) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ResponseClient createAuthorizationCodeClient(AuthorizationCodeRequest request) {
        validateClientNotExists(request.getClientId());

        Client client = createBaseClient(request);

        if ("pkce".equalsIgnoreCase(request.getAuthCodeType())) {
            setupPKCEClient(client, request);
            client.setClientType(ClientType.PKCE);
        } else if ("normal".equalsIgnoreCase(request.getAuthCodeType())) {
            setupNormalAuthCodeClient(client, request);
            client.setClientType(ClientType.NORMAL);
        } else {
            throw new IllegalArgumentException("Invalid auth code type. Must be 'pkce' or 'normal'");
        }

        clientRepository.save(client);
        return createResponse(client, request.getAuthCodeType());
    }

    @Transactional
    public ResponseClient createClientCredentialsClient(ClientCredentialsRequest request) {
        validateClientNotExists(request.getClientId());

        Client client = createBaseClient(request);
        setupClientCredentialsClient(client, request);
        client.setClientType(ClientType.CLIENT_CREDENTIALS);

        clientRepository.save(client);
        return createResponse(client, "client_credentials");
    }

    private Client createBaseClient(CreateClient request) {
        Client client = new Client();
        client.setId(UUID.randomUUID().toString());
        client.setClientId(request.getClientId());
        client.setClientIdIssuedAt(Instant.now());
        client.setClientSecret(passwordEncoder.encode(request.getClientSecret()));
        client.setClientSecretExpiresAt(Instant.now().plus(Duration.ofDays(365)));
        client.setClientName(request.getClientName());
        return client;
    }

    private void setupPKCEClient(Client client, AuthorizationCodeRequest request) {
        // PKCE requires only public client auth or client secret
        List<String> authMethods = List.of(
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue(),
                ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue()
        );
        client.setClientAuthenticationMethods(objectMapper.writeValueAsString(authMethods));

        // Grant types for PKCE
        List<String> grantTypes = List.of(
                AuthorizationGrantType.AUTHORIZATION_CODE.getValue(),
                AuthorizationGrantType.REFRESH_TOKEN.getValue()
        );
        client.setAuthorizationGrantTypes(objectMapper.writeValueAsString(grantTypes));

        // Redirect URIs
        List<String> redirectUris = request.getRedirectUris() != null && !request.getRedirectUris().isEmpty()
                ? request.getRedirectUris()
                : List.of(
                "http://127.0.0.1:9000/login/oauth2/code/" + request.getClientId(),
                "http://localhost:3000/oauth/callback"
        );
        client.setRedirectUris(objectMapper.writeValueAsString(redirectUris));

        // Post logout URIs
        if (request.getPostLogoutRedirectUris() != null) {
            client.setPostLogoutRedirectUris(objectMapper.writeValueAsString(request.getPostLogoutRedirectUris()));
        } else {
            client.setPostLogoutRedirectUris("[]");
        }

        // Scopes
        List<String> scopes = request.getScopes() != null && !request.getScopes().isEmpty()
                ? request.getScopes()
                : List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, "read", "write");
        client.setScopes(objectMapper.writeValueAsString(scopes));

        // Client settings for PKCE (PKCE required)
        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(request.getRequireAuthorizationConsent())
                .requireProofKey(true) // PKCE always requires proof key
                .tokenEndpointAuthenticationSigningAlgorithm(SignatureAlgorithm.RS256)
                .build();
        client.setClientSettings(objectMapper.writeValueAsString(clientSettings.getSettings()));

        // Token settings
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.ofMinutes(request.getAccessTokenTTLMinutes()))
                .refreshTokenTimeToLive(Duration.ofDays(request.getRefreshTokenTTLDays()))
                .reuseRefreshTokens(request.getReuseRefreshTokens())
                .build();
        client.setTokenSettings(objectMapper.writeValueAsString(tokenSettings.getSettings()));

    }

    private void setupNormalAuthCodeClient(Client client, AuthorizationCodeRequest request) {
        // Normal auth code uses client secret
        List<String> authMethods = List.of(
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue(),
                ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue()
        );
        client.setClientAuthenticationMethods(objectMapper.writeValueAsString(authMethods));

        // Grant types
        List<String> grantTypes = List.of(
                AuthorizationGrantType.AUTHORIZATION_CODE.getValue(),
                AuthorizationGrantType.REFRESH_TOKEN.getValue()
        );
        client.setAuthorizationGrantTypes(objectMapper.writeValueAsString(grantTypes));

        // Redirect URIs
        List<String> redirectUris = request.getRedirectUris() != null && !request.getRedirectUris().isEmpty()
                ? request.getRedirectUris()
                : List.of(
                "http://127.0.0.1:9000/login/oauth2/code/" + request.getClientId(),
                "http://localhost:3000/oauth/callback"
        );
        client.setRedirectUris(objectMapper.writeValueAsString(redirectUris));

        // Post logout URIs
        if (request.getPostLogoutRedirectUris() != null) {
            client.setPostLogoutRedirectUris(objectMapper.writeValueAsString(request.getPostLogoutRedirectUris()));
        } else {
            client.setPostLogoutRedirectUris("[]");
        }

        // Scopes
        List<String> scopes = request.getScopes() != null && !request.getScopes().isEmpty()
                ? request.getScopes()
                : List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, "read", "write");
        client.setScopes(objectMapper.writeValueAsString(scopes));

        // Client settings (PKCE optional)
        boolean requireProofKey = request.getRequireProofKey() != null
                ? request.getRequireProofKey()
                : false; // Default to false for normal auth

        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(request.getRequireAuthorizationConsent())
                .requireProofKey(requireProofKey)
                .tokenEndpointAuthenticationSigningAlgorithm(SignatureAlgorithm.RS256)
                .build();
        client.setClientSettings(objectMapper.writeValueAsString(clientSettings.getSettings()));

        // Token settings
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED)
                .accessTokenTimeToLive(Duration.ofMinutes(request.getAccessTokenTTLMinutes()))
                .refreshTokenTimeToLive(Duration.ofDays(request.getRefreshTokenTTLDays()))
                .reuseRefreshTokens(request.getReuseRefreshTokens())
                .build();
        client.setTokenSettings(objectMapper.writeValueAsString(tokenSettings.getSettings()));

    }

    private void setupClientCredentialsClient(Client client, ClientCredentialsRequest request) {
        // Authentication methods for M2M
        List<String> authMethods = List.of(
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue(),
                ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue()
        );
        client.setClientAuthenticationMethods(objectMapper.writeValueAsString(authMethods));

        // Grant types (only client credentials)
        List<String> grantTypes = List.of(
                AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()
        );
        client.setAuthorizationGrantTypes(objectMapper.writeValueAsString(grantTypes));

        // No redirect URIs for M2M
        client.setRedirectUris("[]");
        client.setPostLogoutRedirectUris("[]");

        // Scopes
        List<String> scopes = request.getScopes() != null && !request.getScopes().isEmpty()
                ? request.getScopes()
                : List.of("api.read", "api.write");
        client.setScopes(objectMapper.writeValueAsString(scopes));

        // Client settings for M2M
        ClientSettings clientSettings = ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .requireProofKey(false)
                .build();
        client.setClientSettings(objectMapper.writeValueAsString(clientSettings.getSettings()));

        // Token settings for M2M
        TokenSettings tokenSettings = TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofHours(request.getAccessTokenTTLHours()))
                .reuseRefreshTokens(request.getReuseRefreshTokens())
                .build();
        client.setTokenSettings(objectMapper.writeValueAsString(tokenSettings.getSettings()));

    }

    private void validateClientNotExists(String clientId) {
        clientRepository.findByClientId(clientId)
                .ifPresent(client -> {
                    throw new IllegalArgumentException("Client with ID " + clientId + " already exists");
                });
    }

    private ResponseClient createResponse(Client client, String clientType) {
        ResponseClient response = new ResponseClient();
        response.setId(client.getId());
        response.setClientId(client.getClientId());
        response.setClientName(client.getClientName());
        response.setClientType(clientType.toUpperCase());
        response.setMessage(clientType + " client created successfully");

        List<String> scopes = objectMapper.readValue(client.getScopes(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        response.setScopes(String.join(" ", scopes));

        return response;
    }
}

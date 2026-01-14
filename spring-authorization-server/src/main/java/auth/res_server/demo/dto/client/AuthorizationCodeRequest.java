package auth.res_server.demo.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthorizationCodeRequest extends CreateClient {
    // Client Type: "pkce" or "normal"
    @NotBlank(message = "Auth code type is required (pkce or normal)")
    private String authCodeType = "pkce"; // Default to PKCE

    private List<String> redirectUris;
    private List<String> postLogoutRedirectUris;

    private Boolean requireAuthorizationConsent = true;
    private Boolean requireProofKey = null; // Auto-set based on type
    private Integer accessTokenTTLMinutes = 30;
    private Integer refreshTokenTTLDays = 3;
    private Boolean reuseRefreshTokens = false;
}

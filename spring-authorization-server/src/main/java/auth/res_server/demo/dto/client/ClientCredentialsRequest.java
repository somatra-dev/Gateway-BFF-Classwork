package auth.res_server.demo.dto.client;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ClientCredentialsRequest extends CreateClient {
    private Integer accessTokenTTLHours = 8;
    private Boolean reuseRefreshTokens = true;
}
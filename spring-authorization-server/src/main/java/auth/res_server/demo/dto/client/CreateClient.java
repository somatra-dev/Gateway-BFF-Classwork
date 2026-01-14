package auth.res_server.demo.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateClient{

    @NotBlank(message = "Client ID is required")
    private String clientId;

    @NotBlank(message = "Client name is required")
    private String clientName;

    @NotBlank(message = "Client secret is required")
    private String clientSecret;

    private List<String> scopes;
}
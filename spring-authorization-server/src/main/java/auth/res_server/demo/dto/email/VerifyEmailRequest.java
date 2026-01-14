package auth.res_server.demo.dto.email;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record VerifyEmailRequest(
        @NotBlank(message = "Token is required")
        String token
) {}

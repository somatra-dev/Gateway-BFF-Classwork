package auth.res_server.demo.dto.authority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAuthority(
        @NotBlank(message = "Authority name is required")
        @Pattern(regexp = "^[A-Z_]+$", message = "Authority name must be uppercase with underscores (e.g., READ_USERS)")
        String name,

        String description
) {}

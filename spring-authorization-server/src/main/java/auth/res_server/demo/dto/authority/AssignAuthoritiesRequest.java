package auth.res_server.demo.dto.authority;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AssignAuthoritiesRequest(
        @NotEmpty(message = "Authority names list cannot be empty")
        List<String> authorityNames
) {}

package auth.res_server.demo.dto.role;

import lombok.Builder;

@Builder
public record ResponseRole(
        String id,

        String role_name
) {
}

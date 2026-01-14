package auth.res_server.demo.dto.authority;

import lombok.Builder;

@Builder
public record ResponseAuthority(
        String name,
        String description
) {}

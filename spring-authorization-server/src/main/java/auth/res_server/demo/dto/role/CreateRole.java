package auth.res_server.demo.dto.role;

import lombok.Builder;

@Builder
public record CreateRole (

        String name
){
}

package auth.res_server.demo.dto.user;


import lombok.Builder;

@Builder
public record UpdateUser(

        String username
) {
}

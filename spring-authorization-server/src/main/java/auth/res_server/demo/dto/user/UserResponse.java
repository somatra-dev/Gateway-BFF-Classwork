package auth.res_server.demo.dto.user;


import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record UserResponse(

        String id,
        Long userId,

        String username,
        String email,
        String firstName,
        String lastName,
        String fullName,

        String phoneNumber,
        String gender,
        LocalDate dob,

        String profilePictureUrl,
        String coverImageUrl,

        boolean enabled,
        boolean emailVerified,

        List<String> roles,

        Instant createdAt,
        Instant updatedAt
) {
}

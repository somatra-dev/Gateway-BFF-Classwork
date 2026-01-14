package auth.res_server.demo.controller;

import auth.res_server.demo.dto.BaseResponse;
import auth.res_server.demo.dto.authority.CreateAuthority;
import auth.res_server.demo.dto.authority.ResponseAuthority;
import auth.res_server.demo.service.AuthorityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authorities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuthorityController {

    private final AuthorityService authorityService;

    @PostMapping
    public ResponseEntity<BaseResponse<ResponseAuthority>> createAuthority(
            @Valid @RequestBody CreateAuthority request,
            HttpServletRequest httpRequest) {

        ResponseAuthority response = authorityService.createAuthority(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created(response, httpRequest.getRequestURI()));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResponseAuthority>>> getAllAuthorities(HttpServletRequest httpRequest) {
        List<ResponseAuthority> authorities = authorityService.getAllAuthorities();

        return ResponseEntity.ok(BaseResponse.success(authorities, httpRequest.getRequestURI()));
    }

    @GetMapping("/{name}")
    public ResponseEntity<BaseResponse<ResponseAuthority>> getAuthorityByName(
            @PathVariable String name,
            HttpServletRequest httpRequest) {

        ResponseAuthority authority = authorityService.getAuthorityByName(name);

        return ResponseEntity.ok(BaseResponse.success(authority, httpRequest.getRequestURI()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteAuthority(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        authorityService.deleteAuthority(id);

        return ResponseEntity.ok(BaseResponse.ok(
                "Authority deleted successfully",
                httpRequest.getRequestURI()
        ));
    }
}

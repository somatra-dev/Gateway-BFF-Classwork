package auth.res_server.demo.controller;

import auth.res_server.demo.dto.BaseResponse;
import auth.res_server.demo.dto.authority.AssignAuthoritiesRequest;
import auth.res_server.demo.dto.authority.ResponseAuthority;
import auth.res_server.demo.dto.role.CreateRole;
import auth.res_server.demo.dto.role.ResponseRole;
import auth.res_server.demo.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createRole(
            @Valid @RequestBody CreateRole request,
            HttpServletRequest httpRequest) {

        roleService.createRole(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        "Role created successfully",
                        HttpStatus.CREATED.value(),
                        httpRequest.getRequestURI()
                ));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ResponseRole>>> getAllRoles(HttpServletRequest httpRequest) {
        List<ResponseRole> roles = roleService.getAllRoles();

        return ResponseEntity.ok(BaseResponse.success(roles, httpRequest.getRequestURI()));
    }

    @GetMapping("/{name}")
    public ResponseEntity<BaseResponse<ResponseRole>> getRoleByName(
            @PathVariable String name,
            HttpServletRequest httpRequest) {

        ResponseRole role = roleService.getRoleByName(name);

        return ResponseEntity.ok(BaseResponse.success(role, httpRequest.getRequestURI()));
    }

    @GetMapping("/{roleId}/authorities")
    public ResponseEntity<BaseResponse<List<ResponseAuthority>>> getAuthoritiesByRole(
            @PathVariable Long roleId,
            HttpServletRequest httpRequest) {

        List<ResponseAuthority> authorities = roleService.getAuthoritiesByRole(roleId);

        return ResponseEntity.ok(BaseResponse.success(authorities, httpRequest.getRequestURI()));
    }

    @PostMapping("/{roleId}/authorities")
    public ResponseEntity<BaseResponse<List<ResponseAuthority>>> assignAuthoritiesToRole(
            @PathVariable Long roleId,
            @Valid @RequestBody AssignAuthoritiesRequest request,
            HttpServletRequest httpRequest) {

        List<ResponseAuthority> authorities = roleService.assignAuthoritiesToRole(roleId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        authorities,
                        "Authorities assigned successfully",
                        HttpStatus.CREATED.value(),
                        httpRequest.getRequestURI()
                ));
    }

    @DeleteMapping("/{roleId}/authorities/{authorityId}")
    public ResponseEntity<BaseResponse<Void>> removeAuthorityFromRole(
            @PathVariable Long roleId,
            @PathVariable Long authorityId,
            HttpServletRequest httpRequest) {

        roleService.removeAuthorityFromRole(roleId, authorityId);

        return ResponseEntity.ok(BaseResponse.ok(
                "Authority removed from role successfully",
                httpRequest.getRequestURI()
        ));
    }
}

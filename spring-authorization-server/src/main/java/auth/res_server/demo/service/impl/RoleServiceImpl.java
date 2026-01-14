package auth.res_server.demo.service.impl;

import auth.res_server.demo.domain.Authority;
import auth.res_server.demo.domain.Role;
import auth.res_server.demo.dto.authority.AssignAuthoritiesRequest;
import auth.res_server.demo.dto.authority.ResponseAuthority;
import auth.res_server.demo.dto.role.CreateRole;
import auth.res_server.demo.dto.role.ResponseRole;
import auth.res_server.demo.repository.AuthorityRepository;
import auth.res_server.demo.repository.RoleRepository;
import auth.res_server.demo.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    public void createRole(CreateRole createRole) {
        String roleName = toFullRoleName(createRole.name());

        if (roleRepository.existsByName(roleName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role already exists: " + roleName);
        }

        Role role = new Role();
        role.setName(roleName);
        roleRepository.save(role);
    }

    @Override
    public List<ResponseRole> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::toResponseRole)
                .toList();
    }

    @Override
    public ResponseRole getRoleByName(String name) {
        String roleName = toFullRoleName(name);
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + roleName));
        return toResponseRole(role);
    }

    @Override
    public List<ResponseAuthority> assignAuthoritiesToRole(Long roleId, AssignAuthoritiesRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with id: " + roleId));

        List<Authority> authoritiesToAdd = request.authorityNames().stream()
                .map(name -> authorityRepository.findByName(name.toUpperCase())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authority not found: " + name)))
                .toList();

        // Add authorities that are not already assigned
        for (Authority authority : authoritiesToAdd) {
            if (!role.getAuthorities().contains(authority)) {
                role.getAuthorities().add(authority);
            }
        }

        roleRepository.save(role);

        return role.getAuthorities().stream()
                .map(this::toResponseAuthority)
                .toList();
    }

    @Override
    public void removeAuthorityFromRole(Long roleId, Long authorityId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with id: " + roleId));

        Authority authority = authorityRepository.findById(authorityId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authority not found with id: " + authorityId));

        if (!role.getAuthorities().remove(authority)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authority is not assigned to this role");
        }

        roleRepository.save(role);
    }

    @Override
    public List<ResponseAuthority> getAuthoritiesByRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with id: " + roleId));

        return role.getAuthorities().stream()
                .map(this::toResponseAuthority)
                .toList();
    }

    private String toFullRoleName(String input) {
        if (input == null) return null;
        String trimmed = input.trim().toUpperCase();
        return trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed;
    }

    private ResponseRole toResponseRole(Role role) {
        return ResponseRole.builder()
                .id(role.getId().toString())
                .role_name(role.getName())
                .build();
    }

    private ResponseAuthority toResponseAuthority(Authority authority) {
        return ResponseAuthority.builder()
                .name(authority.getName())
                .description(authority.getDescription())
                .build();
    }
}

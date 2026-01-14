package auth.res_server.demo.service;

import auth.res_server.demo.dto.authority.AssignAuthoritiesRequest;
import auth.res_server.demo.dto.authority.ResponseAuthority;
import auth.res_server.demo.dto.role.CreateRole;
import auth.res_server.demo.dto.role.ResponseRole;

import java.util.List;

public interface RoleService {

    void createRole(CreateRole createRole);

    List<ResponseRole> getAllRoles();

    ResponseRole getRoleByName(String name);

    List<ResponseAuthority> assignAuthoritiesToRole(Long roleId, AssignAuthoritiesRequest request);

    void removeAuthorityFromRole(Long roleId, Long authorityId);

    List<ResponseAuthority> getAuthoritiesByRole(Long roleId);
}

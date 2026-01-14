package auth.res_server.demo.service;

import auth.res_server.demo.dto.authority.CreateAuthority;
import auth.res_server.demo.dto.authority.ResponseAuthority;

import java.util.List;

public interface AuthorityService {

    ResponseAuthority createAuthority(CreateAuthority request);

    List<ResponseAuthority> getAllAuthorities();

    ResponseAuthority getAuthorityByName(String name);

    void deleteAuthority(Long id);
}

package auth.res_server.demo.service.impl;

import auth.res_server.demo.domain.Authority;
import auth.res_server.demo.dto.authority.CreateAuthority;
import auth.res_server.demo.dto.authority.ResponseAuthority;
import auth.res_server.demo.repository.AuthorityRepository;
import auth.res_server.demo.service.AuthorityService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorityServiceImpl implements AuthorityService {

    private final AuthorityRepository authorityRepository;

    @Override
    public ResponseAuthority createAuthority(CreateAuthority request) {
        if (authorityRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Authority already exists: " + request.name());
        }

        Authority authority = new Authority();
        authority.setName(request.name().toUpperCase());
        authority.setDescription(request.description());

        Authority saved = authorityRepository.save(authority);

        return toResponse(saved);
    }

    @Override
    public List<ResponseAuthority> getAllAuthorities() {
        return authorityRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public ResponseAuthority getAuthorityByName(String name) {
        Authority authority = authorityRepository.findByName(name.toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authority not found: " + name));
        return toResponse(authority);
    }

    @Override
    public void deleteAuthority(Long id) {
        if (!authorityRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Authority not found with id: " + id);
        }
        authorityRepository.deleteById(id);
    }

    private ResponseAuthority toResponse(Authority authority) {
        return ResponseAuthority.builder()
                .name(authority.getName())
                .description(authority.getDescription())
                .build();
    }
}

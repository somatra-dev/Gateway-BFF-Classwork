package auth.res_server.demo.controller;

import auth.res_server.demo.dto.BaseResponse;
import auth.res_server.demo.dto.client.AuthorizationCodeRequest;
import auth.res_server.demo.dto.client.ClientCredentialsRequest;
import auth.res_server.demo.dto.client.ResponseClient;
import auth.res_server.demo.service.ClientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/pkce")
    public ResponseEntity<BaseResponse<ResponseClient>> createPKCEClient(
            @Valid @RequestBody AuthorizationCodeRequest request,
            HttpServletRequest httpRequest) {

        request.setAuthCodeType("pkce");
        request.setRequireProofKey(true);
        ResponseClient response = clientService.createAuthorizationCodeClient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created(response, httpRequest.getRequestURI()));
    }

    @PostMapping("/normal-auth")
    public ResponseEntity<BaseResponse<ResponseClient>> createNormalAuthClient(
            @Valid @RequestBody AuthorizationCodeRequest request,
            HttpServletRequest httpRequest) {

        request.setAuthCodeType("normal");
        ResponseClient response = clientService.createAuthorizationCodeClient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created(response, httpRequest.getRequestURI()));
    }

    @PostMapping("/client-credentials")
    public ResponseEntity<BaseResponse<ResponseClient>> createClientCredentialsClient(
            @Valid @RequestBody ClientCredentialsRequest request,
            HttpServletRequest httpRequest) {

        ResponseClient response = clientService.createClientCredentialsClient(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.created(response, httpRequest.getRequestURI()));
    }
}

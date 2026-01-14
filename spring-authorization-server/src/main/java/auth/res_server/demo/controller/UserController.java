package auth.res_server.demo.controller;

import auth.res_server.demo.dto.BaseResponse;
import auth.res_server.demo.dto.user.CreateUser;
import auth.res_server.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<BaseResponse<Void>> createUser(
            @Valid @RequestBody CreateUser request,
            HttpServletRequest httpRequest) {

        userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        "User created successfully",
                        HttpStatus.CREATED.value(),
                        httpRequest.getRequestURI()
                ));
    }
}

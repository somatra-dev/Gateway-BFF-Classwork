package auth.res_server.demo.controller;

import auth.res_server.demo.dto.BaseResponse;
import auth.res_server.demo.dto.email.ResendVerificationRequest;
import auth.res_server.demo.dto.email.VerifyEmailRequest;
import auth.res_server.demo.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<BaseResponse<Void>> verifyEmail(
            @RequestParam String token,
            HttpServletRequest httpRequest) {

        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(BaseResponse.ok(
                "Email verified successfully",
                httpRequest.getRequestURI()
        ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<BaseResponse<Void>> verifyEmailPost(
            @Valid @RequestBody VerifyEmailRequest request,
            HttpServletRequest httpRequest) {

        emailVerificationService.verifyEmail(request.token());

        return ResponseEntity.ok(BaseResponse.ok(
                "Email verified successfully",
                httpRequest.getRequestURI()
        ));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<BaseResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest httpRequest) {

        emailVerificationService.resendVerificationEmail(request.email());

        return ResponseEntity.ok(BaseResponse.ok(
                "If the email exists, a verification email has been sent",
                httpRequest.getRequestURI()
        ));
    }
}

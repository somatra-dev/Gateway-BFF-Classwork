package auth.res_server.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final RegisteredClientRepository registeredClientRepository;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/oauth2/consent")
    public String consent(
            Principal principal,
            Model model,
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(OAuth2ParameterNames.SCOPE) String scope,
            @RequestParam(OAuth2ParameterNames.STATE) String state) {

        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);

        Set<String> scopesToApprove = new HashSet<>(Arrays.asList(scope.split(" ")));

        model.addAttribute("clientId", clientId);
        model.addAttribute("clientName", registeredClient != null ? registeredClient.getClientName() : clientId);
        model.addAttribute("state", state);
        model.addAttribute("scopes", scopesToApprove);
        model.addAttribute("principalName", principal.getName());

        return "consent";
    }

    @GetMapping("/logout")
    public String logoutPage(@RequestParam(value = "post_logout_redirect_uri", required = false) String redirectUri) {
        // This is the logout confirmation page
        // You can auto-confirm or show a confirmation page

        // Option 1: Auto-confirm and redirect
        if (redirectUri != null) {
            // Redirect to the provided URI
            return "redirect:" + redirectUri;
        }

        // Option 2: Show confirmation page
        return "logout-confirm";
    }
}

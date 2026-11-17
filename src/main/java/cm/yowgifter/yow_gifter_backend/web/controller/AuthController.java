package cm.yowgifter.yow_gifter_backend.web.controller;

import cm.yowgifter.yow_gifter_backend.service.AuthService;
import cm.yowgifter.yow_gifter_backend.web.dto.AuthResponse;
import cm.yowgifter.yow_gifter_backend.web.dto.LoginRequest;
import cm.yowgifter.yow_gifter_backend.web.dto.RegisterRequest;
import cm.yowgifter.yow_gifter_backend.web.dto.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints d'inscription, connexion et récupération de session JWT")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Inscription citoyenne d'un nouveau membre (Donateur / Bénéficiaire)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un membre et obtention du Bearer JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtention des informations du compte actuellement connecté")
    public ResponseEntity<UserDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }
}

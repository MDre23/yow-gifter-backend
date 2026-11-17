package cm.yowgifter.yow_gifter_backend.web.controller;

import cm.yowgifter.yow_gifter_backend.service.AuthService;
import cm.yowgifter.yow_gifter_backend.web.dto.UserDto;
import cm.yowgifter.yow_gifter_backend.web.dto.UserPublicDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion du profil personnel et consultation publique protégée")
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Consulter son profil privé complet")
    public ResponseEntity<UserDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getCurrentUser(userDetails.getUsername()));
    }

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour ses informations de profil (prénom, quartier, bio, avatar)")
    public ResponseEntity<UserDto> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto updateDto
    ) {
        return ResponseEntity.ok(authService.updateProfile(userDetails.getUsername(), updateDto));
    }

    @GetMapping("/{id}/public")
    @Operation(summary = "Consulter le profil public d'un membre (Conforme REQ-F07 : données civiles masquées)")
    public ResponseEntity<UserPublicDto> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(authService.getPublicUser(id));
    }
}

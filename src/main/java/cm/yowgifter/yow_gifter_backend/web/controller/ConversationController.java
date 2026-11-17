package cm.yowgifter.yow_gifter_backend.web.controller;

import cm.yowgifter.yow_gifter_backend.service.MessagingService;
import cm.yowgifter.yow_gifter_backend.web.dto.ConversationDto;
import cm.yowgifter.yow_gifter_backend.web.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Messagerie Privée Bilatérale", description = "Échanges sécurisés entre le donateur et le bénéficiaire prioritaire")
public class ConversationController {

    private final MessagingService messagingService;

    @GetMapping
    @Operation(summary = "Lister toutes mes conversations privées")
    public ResponseEntity<List<ConversationDto>> getMyConversations(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messagingService.getUserConversations(userDetails.getUsername()));
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Consulter le fil de messages d'une conversation")
    public ResponseEntity<List<MessageDto>> getMessages(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(messagingService.getMessages(id, userDetails.getUsername()));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Envoyer un message privé pour fixer le rendez-vous solidaire")
    public ResponseEntity<MessageDto> sendMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String content = body.get("content");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messagingService.sendMessage(id, content, userDetails.getUsername()));
    }
}

package cm.yowgifter.yow_gifter_backend.web.controller;

import cm.yowgifter.yow_gifter_backend.service.RequestFifoService;
import cm.yowgifter.yow_gifter_backend.web.dto.RequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "File d'attente FIFO & Demandes", description = "Algorithme premier arrivé premier servi, TTL et désistement")
public class RequestController {

    private final RequestFifoService requestFifoService;

    @PostMapping("/api/v1/donations/{id}/requests")
    @Operation(summary = "Rejoindre la file d'attente pour un don (Horodatage serveur strict REQ-F09)")
    public ResponseEntity<RequestDto> joinQueue(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestFifoService.joinQueue(id, userDetails.getUsername()));
    }

    @GetMapping("/api/v1/donations/{id}/requests")
    @Operation(summary = "Consulter l'état de la file d'attente pour un don donné")
    public ResponseEntity<List<RequestDto>> getQueueForDonation(@PathVariable Long id) {
        return ResponseEntity.ok(requestFifoService.getQueueForDonation(id));
    }

    @DeleteMapping("/api/v1/requests/{id}")
    @Operation(summary = "Se désister d'une demande (Promotion automatique du suivant REQ-F11)")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        requestFifoService.cancelRequest(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/users/me/requests")
    @Operation(summary = "Consulter la liste de toutes mes demandes en cours et passées")
    public ResponseEntity<List<RequestDto>> getMyRequests(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(requestFifoService.getUserRequests(userDetails.getUsername()));
    }
}

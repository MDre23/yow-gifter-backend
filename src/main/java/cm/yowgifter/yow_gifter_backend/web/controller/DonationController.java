package cm.yowgifter.yow_gifter_backend.web.controller;

import cm.yowgifter.yow_gifter_backend.domain.enums.DonationStatus;
import cm.yowgifter.yow_gifter_backend.service.DonationService;
import cm.yowgifter.yow_gifter_backend.web.dto.CreateDonationRequest;
import cm.yowgifter.yow_gifter_backend.web.dto.DonationDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
@Tag(name = "Annonces de Dons", description = "Publication, recherche et gestion des dons 100% gratuits")
public class DonationController {

    private final DonationService donationService;

    @GetMapping
    @Operation(summary = "Lister les dons disponibles avec filtres (catégorie, quartier, recherche)")
    public ResponseEntity<Page<DonationDto>> getAvailableDonations(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String neighborhood,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(donationService.getAvailableDonations(categoryId, neighborhood, q, pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter le détail complet d'une annonce de don")
    public ResponseEntity<DonationDto> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationService.getDonationById(id));
    }

    @PostMapping
    @Operation(summary = "Publier une nouvelle annonce de don solidaire (0 FCFA)")
    public ResponseEntity<DonationDto> createDonation(
            @Valid @RequestBody CreateDonationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(donationService.createDonation(request, userDetails.getUsername()));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Ajouter des photos à une annonce (Contrôle strict REQ-F04 : Refus à la 3ᵉ photo)")
    public ResponseEntity<DonationDto> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(donationService.addImages(id, files, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Supprimer une photo d'une annonce de don")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        donationService.deleteImage(id, imageId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Modifier le statut d'une annonce (AVAILABLE, RESERVED, GIVEN, SUSPENDED)")
    public ResponseEntity<DonationDto> updateStatus(
            @PathVariable Long id,
            @RequestParam DonationStatus status,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(donationService.updateStatus(id, status, userDetails.getUsername()));
    }
}

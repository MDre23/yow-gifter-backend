package cm.yowgifter.yow_gifter_backend.service;

import cm.yowgifter.yow_gifter_backend.domain.Category;
import cm.yowgifter.yow_gifter_backend.domain.Donation;
import cm.yowgifter.yow_gifter_backend.domain.DonationImage;
import cm.yowgifter.yow_gifter_backend.domain.User;
import cm.yowgifter.yow_gifter_backend.domain.enums.DonationStatus;
import cm.yowgifter.yow_gifter_backend.domain.enums.RequestStatus;
import cm.yowgifter.yow_gifter_backend.repository.CategoryRepository;
import cm.yowgifter.yow_gifter_backend.repository.DonationImageRepository;
import cm.yowgifter.yow_gifter_backend.repository.DonationRepository;
import cm.yowgifter.yow_gifter_backend.repository.RequestRepository;
import cm.yowgifter.yow_gifter_backend.repository.UserRepository;
import cm.yowgifter.yow_gifter_backend.web.dto.CreateDonationRequest;
import cm.yowgifter.yow_gifter_backend.web.dto.DonationDto;
import cm.yowgifter.yow_gifter_backend.web.dto.UserPublicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final DonationImageRepository donationImageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final FileStorageService fileStorageService;

    public Page<DonationDto> getAvailableDonations(Long categoryId, String neighborhood, String keyword, Pageable pageable) {
        return donationRepository.findAvailableWithFilters(categoryId, neighborhood, keyword, pageable)
                .map(this::mapToDonationDto);
    }

    public DonationDto getDonationById(Long id) {
        Donation donation = donationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Don introuvable avec l'identifiant : " + id));
        return mapToDonationDto(donation);
    }

    public List<DonationDto> getDonationsByDonor(String userEmail) {
        User donor = getUserByEmail(userEmail);
        return donationRepository.findByDonorIdOrderByCreatedAtDesc(donor.getId())
                .stream()
                .map(this::mapToDonationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DonationDto createDonation(CreateDonationRequest request, String userEmail) {
        User donor = getUserByEmail(userEmail);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie inexistante."));

        Donation donation = Donation.builder()
                .donor(donor)
                .category(category)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .condition(request.getCondition())
                .status(DonationStatus.AVAILABLE)
                .city(request.getCity() != null ? request.getCity() : donor.getCity())
                .neighborhood(request.getNeighborhood() != null ? request.getNeighborhood() : donor.getNeighborhood())
                .quantity(request.getQuantity() != null && request.getQuantity() > 0 ? request.getQuantity() : 1)
                .images(new ArrayList<>())
                .build();

        donation = donationRepository.save(donation);
        return mapToDonationDto(donation);
    }

    /**
     * Règle critique REQ-F04 : Refus systématique de toute photo au-delà de 2 par annonce.
     */
    @Transactional
    public DonationDto addImages(Long donationId, List<MultipartFile> files, String userEmail) {
        Donation donation = getDonationAndVerifyOwnership(donationId, userEmail);

        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun fichier fourni.");
        }

        long existingCount = donationImageRepository.countByDonationId(donationId);
        if (existingCount + files.size() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Règle de sobriété (REQ-F04) enfreinte : Le don compte déjà " + existingCount +
                    " photo(s). L'ajout de " + files.size() + " photo(s) dépasse la limite absolue de 2 photos maximum."
            );
        }

        for (MultipartFile file : files) {
            String imageUrl = fileStorageService.storeFile(file);
            DonationImage image = DonationImage.builder()
                    .donation(donation)
                    .imageUrl(imageUrl)
                    .isPrimary(existingCount == 0) // La première devient principale
                    .build();
            donationImageRepository.save(image);
            existingCount++;
        }

        return mapToDonationDto(donationRepository.findById(donationId).get());
    }

    @Transactional
    public void deleteImage(Long donationId, Long imageId, String userEmail) {
        getDonationAndVerifyOwnership(donationId, userEmail);

        DonationImage image = donationImageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo introuvable."));

        if (!image.getDonation().getId().equals(donationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette photo n'appartient pas à cette annonce.");
        }

        donationImageRepository.delete(image);
    }

    @Transactional
    public DonationDto updateStatus(Long donationId, DonationStatus newStatus, String userEmail) {
        Donation donation = getDonationAndVerifyOwnership(donationId, userEmail);
        donation.setStatus(newStatus);
        donation = donationRepository.save(donation);
        return mapToDonationDto(donation);
    }

    private Donation getDonationAndVerifyOwnership(Long donationId, String userEmail) {
        Donation donation = donationRepository.findById(donationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Don introuvable."));

        if (!donation.getDonor().getEmail().equalsIgnoreCase(userEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul le donateur propriétaire de l'annonce est autorisé à effectuer cette action.");
        }
        return donation;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
    }

    public DonationDto mapToDonationDto(Donation donation) {
        List<String> imageUrls = donationImageRepository.findByDonationIdOrderByCreatedAtAsc(donation.getId())
                .stream()
                .map(DonationImage::getImageUrl)
                .collect(Collectors.toList());

        long waitingCount = requestRepository.countByDonationIdAndStatus(donation.getId(), RequestStatus.WAITING);

        User donor = donation.getDonor();
        UserPublicDto donorPublic = UserPublicDto.builder()
                .id(donor.getId())
                .username(donor.getUsername())
                .firstName(donor.getFirstName())
                .city(donor.getCity())
                .neighborhood(donor.getNeighborhood())
                .age(donor.getAge())
                .bio(donor.getBio())
                .avatarUrl(donor.getAvatarUrl())
                .isVerified(donor.getIsVerified())
                .createdAt(donor.getCreatedAt())
                .build();

        return DonationDto.builder()
                .id(donation.getId())
                .title(donation.getTitle())
                .description(donation.getDescription())
                .condition(donation.getCondition())
                .status(donation.getStatus())
                .city(donation.getCity())
                .neighborhood(donation.getNeighborhood())
                .quantity(donation.getQuantity())
                .categoryId(donation.getCategory().getId())
                .categoryName(donation.getCategory().getName())
                .categorySlug(donation.getCategory().getSlug())
                .donor(donorPublic)
                .imageUrls(imageUrls)
                .waitingRequestsCount((int) waitingCount)
                .createdAt(donation.getCreatedAt())
                .updatedAt(donation.getUpdatedAt())
                .build();
    }
}

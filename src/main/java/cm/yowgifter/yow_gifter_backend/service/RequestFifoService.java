package cm.yowgifter.yow_gifter_backend.service;

import cm.yowgifter.yow_gifter_backend.domain.Conversation;
import cm.yowgifter.yow_gifter_backend.domain.Donation;
import cm.yowgifter.yow_gifter_backend.domain.Request;
import cm.yowgifter.yow_gifter_backend.domain.User;
import cm.yowgifter.yow_gifter_backend.domain.enums.DonationStatus;
import cm.yowgifter.yow_gifter_backend.domain.enums.RequestStatus;
import cm.yowgifter.yow_gifter_backend.repository.ConversationRepository;
import cm.yowgifter.yow_gifter_backend.repository.DonationRepository;
import cm.yowgifter.yow_gifter_backend.repository.RequestRepository;
import cm.yowgifter.yow_gifter_backend.repository.UserRepository;
import cm.yowgifter.yow_gifter_backend.web.dto.RequestDto;
import cm.yowgifter.yow_gifter_backend.web.dto.UserPublicDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestFifoService {

    private final RequestRepository requestRepository;
    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    @Value("${app.fifo.ttl-hours:48}")
    private long ttlHours;

    /**
     * REQ-F09 & REQ-F10 : Rejoindre la file d'attente FIFO.
     * Horodatage strictement serveur. Un seul bénéficiaire prioritaire à la fois.
     */
    @Transactional
    public RequestDto joinQueue(Long donationId, String userEmail) {
        // 🛠️ VERROUILLAGE PESSIMISTE : On bloque la ligne pour garantir l'ordre séquentiel des t_i
        Donation donation = donationRepository.findByIdWithLock(donationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Don introuvable."));

        if (donation.getStatus() == DonationStatus.GIVEN || donation.getStatus() == DonationStatus.SUSPENDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce don n'est plus disponible.");
        }

        User beneficiary = getUserByEmail(userEmail);

        if (donation.getDonor().getId().equals(beneficiary.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un donateur ne peut pas demander son propre don.");
        }

        // Vérifier si déjà dans la file (actif)
        Optional<Request> existing = requestRepository.findByDonationIdAndBeneficiaryId(donationId, beneficiary.getId());
        if (existing.isPresent() && (existing.get().getStatus() == RequestStatus.WAITING || existing.get().getStatus() == RequestStatus.PRIORITY)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous êtes déjà inscrit dans la file d'attente de cet objet.");
        }

        // Génération du t_i (timestamp ultra précis). 
        // Grâce au verrou, les utilisateurs seront traités strictement l'un après l'autre.
        Instant serverTimestamp = Instant.now();

        // Vérifier s'il y a déjà un prioritaire actif
        Optional<Request> activePriority = requestRepository.findByDonationIdAndStatus(donationId, RequestStatus.PRIORITY);

        Request request = Request.builder()
                .donation(donation)
                .beneficiary(beneficiary)
                .requestedAt(serverTimestamp)
                .build();

        if (activePriority.isEmpty()) {
            // Premier demandeur : il devient immédiatement PRIORITAIRE avec un compte à rebours (TTL)
            request.setStatus(RequestStatus.PRIORITY);
            request.setExpiresAt(serverTimestamp.plus(ttlHours, ChronoUnit.HOURS));
            donation.setStatus(DonationStatus.RESERVED);
            donationRepository.save(donation);

            // Création de la conversation privée bilatérale pour le rendez-vous
            createConversationIfNotExist(donation, donation.getDonor(), beneficiary);
        } else {
            // Demandeur suivant : positionné dans la file d'attente FIFO
            request.setStatus(RequestStatus.WAITING);
        }

        request = requestRepository.save(request);

        return mapToRequestDto(request, getQueuePosition(request));
    }

    /**
     * REQ-F11 : Désistement volontaire du bénéficiaire.
     */
    @Transactional
    public void cancelRequest(Long requestId, String userEmail) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Demande introuvable."));

        User user = getUserByEmail(userEmail);

        if (!request.getBeneficiary().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Action non autorisée.");
        }

        boolean wasPriority = (request.getStatus() == RequestStatus.PRIORITY);
        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        if (wasPriority) {
            log.info("Désistement du bénéficiaire prioritaire pour le don {}. Promotion du suivant...", request.getDonation().getId());
            promoteNextInQueue(request.getDonation());
        }
    }

    /**
     * REQ-F12 : Expiration automatique TTL du bénéficiaire prioritaire.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkTtlExpirations() {
        Instant now = Instant.now();
        List<Request> expiredRequests = requestRepository.findByStatusAndExpiresAtBefore(RequestStatus.PRIORITY, now);

        for (Request req : expiredRequests) {
            log.info("Expiration du TTL pour la demande {} (Don {}).", req.getId(), req.getDonation().getId());
            req.setStatus(RequestStatus.EXPIRED);
            requestRepository.save(req);
            promoteNextInQueue(req.getDonation());
        }
    }

    /**
     * Promotion automatique du prochain demandeur dans l'ordre FIFO (requested_at ASC).
     */
    @Transactional
    public void promoteNextInQueue(Donation donation) {
        List<Request> queue = requestRepository.findByDonationIdOrderByRequestedAtAsc(donation.getId());

        Optional<Request> nextInLine = queue.stream()
                .filter(r -> r.getStatus() == RequestStatus.WAITING)
                .findFirst();

        if (nextInLine.isPresent()) {
            Request promoted = nextInLine.get();
            promoted.setStatus(RequestStatus.PRIORITY);
            promoted.setExpiresAt(Instant.now().plus(ttlHours, ChronoUnit.HOURS));
            requestRepository.save(promoted);

            donation.setStatus(DonationStatus.RESERVED);
            donationRepository.save(donation);

            createConversationIfNotExist(donation, donation.getDonor(), promoted.getBeneficiary());
            log.info("Bénéficiaire {} promu prioritaire pour le don {}.", promoted.getBeneficiary().getUsername(), donation.getId());
        } else {
            // Personne d'autre dans la file : le don redevient disponible
            donation.setStatus(DonationStatus.AVAILABLE);
            donationRepository.save(donation);
            log.info("Aucun autre demandeur. Le don {} redevient AVAILABLE.", donation.getId());
        }
    }

    public List<RequestDto> getQueueForDonation(Long donationId) {
        List<Request> list = requestRepository.findByDonationIdOrderByRequestedAtAsc(donationId);
        List<RequestDto> result = new ArrayList<>();
        int pos = 1;
        for (Request r : list) {
            if (r.getStatus() == RequestStatus.PRIORITY || r.getStatus() == RequestStatus.WAITING) {
                result.add(mapToRequestDto(r, pos++));
            }
        }
        return result;
    }

    public List<RequestDto> getUserRequests(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Request> userRequests = requestRepository.findByBeneficiaryIdOrderByCreatedAtDesc(user.getId());
        List<RequestDto> dtos = new ArrayList<>();
        for (Request r : userRequests) {
            dtos.add(mapToRequestDto(r, getQueuePosition(r)));
        }
        return dtos;
    }

    /**
     * 🛠️ APPLICATION DE LA FORMULE MATHÉMATIQUE :
     * P(r_i) = 1 + Somme(I(t_j < t_i))
     */
    private int getQueuePosition(Request target) {
        if (target.getStatus() == RequestStatus.PRIORITY) return 1;
        if (target.getStatus() != RequestStatus.WAITING) return 0;

        // Appel à PostgreSQL optimisé par index : compte le nombre de t_j < t_i
        int countBefore = requestRepository.countRequestsBefore(
                target.getDonation().getId(), 
                target.getRequestedAt()
        );
        
        return 1 + countBefore;
    }

    private void createConversationIfNotExist(Donation donation, User donor, User beneficiary) {
        conversationRepository.findByDonationIdAndBeneficiaryId(donation.getId(), beneficiary.getId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .donation(donation)
                                .donor(donor)
                                .beneficiary(beneficiary)
                                .build()
                ));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
    }

    private RequestDto mapToRequestDto(Request request, int position) {
        User b = request.getBeneficiary();
        UserPublicDto beneficiaryPublic = UserPublicDto.builder()
                .id(b.getId())
                .username(b.getUsername())
                .firstName(b.getFirstName())
                .city(b.getCity())
                .neighborhood(b.getNeighborhood())
                .avatarUrl(b.getAvatarUrl())
                .isVerified(b.getIsVerified())
                .build();

        return RequestDto.builder()
                .id(request.getId())
                .donationId(request.getDonation().getId())
                .donationTitle(request.getDonation().getTitle())
                .beneficiaryId(b.getId())
                .beneficiary(beneficiaryPublic)
                .status(request.getStatus())
                .positionInQueue(position)
                .requestedAt(request.getRequestedAt())
                .expiresAt(request.getExpiresAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
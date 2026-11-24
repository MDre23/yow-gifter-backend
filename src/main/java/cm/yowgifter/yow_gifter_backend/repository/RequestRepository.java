package cm.yowgifter.yow_gifter_backend.repository;

import cm.yowgifter.yow_gifter_backend.domain.Request;
import cm.yowgifter.yow_gifter_backend.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByDonationIdOrderByRequestedAtAsc(Long donationId);

    Optional<Request> findByDonationIdAndStatus(Long donationId, RequestStatus status);

    Optional<Request> findByDonationIdAndBeneficiaryId(Long donationId, Long beneficiaryId);

    List<Request> findByBeneficiaryIdOrderByCreatedAtDesc(Long beneficiaryId);

    List<Request> findByStatusAndExpiresAtBefore(RequestStatus status, Instant now);

    long countByDonationIdAndStatus(Long donationId, RequestStatus status);

    // 🛠️ LA FORMULE MATHÉMATIQUE : 
    // Calcule le nombre de requêtes actives (WAITING ou PRIORITY) dont le timestamp est STRICTEMENT INFÉRIEUR
    @Query("SELECT COUNT(r) FROM Request r WHERE r.donation.id = :donationId " +
           "AND r.status IN ('WAITING', 'PRIORITY') " +
           "AND r.requestedAt < :targetTime")
    int countRequestsBefore(@Param("donationId") Long donationId, @Param("targetTime") Instant targetTime);
}
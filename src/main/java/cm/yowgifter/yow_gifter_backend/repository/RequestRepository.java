package cm.yowgifter.yow_gifter_backend.repository;

import cm.yowgifter.yow_gifter_backend.domain.Request;
import cm.yowgifter.yow_gifter_backend.domain.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

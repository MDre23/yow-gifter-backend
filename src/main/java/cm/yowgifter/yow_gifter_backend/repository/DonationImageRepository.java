package cm.yowgifter.yow_gifter_backend.repository;

import cm.yowgifter.yow_gifter_backend.domain.DonationImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationImageRepository extends JpaRepository<DonationImage, Long> {
    List<DonationImage> findByDonationIdOrderByCreatedAtAsc(Long donationId);
    long countByDonationId(Long donationId);
}

package cm.yowgifter.yow_gifter_backend.repository;

import cm.yowgifter.yow_gifter_backend.domain.Donation;
import cm.yowgifter.yow_gifter_backend.domain.enums.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    Page<Donation> findByStatusOrderByCreatedAtDesc(DonationStatus status, Pageable pageable);

    @Query("SELECT d FROM Donation d WHERE d.status = 'AVAILABLE' " +
           "AND (:categoryId IS NULL OR d.category.id = :categoryId) " +
           "AND (:neighborhood IS NULL OR LOWER(d.neighborhood) LIKE LOWER(CONCAT('%', :neighborhood, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.createdAt DESC")
    Page<Donation> findAvailableWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("neighborhood") String neighborhood,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}

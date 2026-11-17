package cm.yowgifter.yow_gifter_backend.repository;

import cm.yowgifter.yow_gifter_backend.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE c.donor.id = :userId OR c.beneficiary.id = :userId ORDER BY c.createdAt DESC")
    List<Conversation> findUserConversations(@Param("userId") Long userId);

    Optional<Conversation> findByDonationIdAndBeneficiaryId(Long donationId, Long beneficiaryId);
}

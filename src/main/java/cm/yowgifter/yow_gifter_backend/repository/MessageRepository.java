package cm.yowgifter.yow_gifter_backend.repository;

import cm.yowgifter.yow_gifter_backend.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);
}

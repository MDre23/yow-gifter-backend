package cm.yowgifter.yow_gifter_backend.service;

import cm.yowgifter.yow_gifter_backend.domain.Conversation;
import cm.yowgifter.yow_gifter_backend.domain.Message;
import cm.yowgifter.yow_gifter_backend.domain.User;
import cm.yowgifter.yow_gifter_backend.repository.ConversationRepository;
import cm.yowgifter.yow_gifter_backend.repository.MessageRepository;
import cm.yowgifter.yow_gifter_backend.repository.UserRepository;
import cm.yowgifter.yow_gifter_backend.web.dto.ConversationDto;
import cm.yowgifter.yow_gifter_backend.web.dto.MessageDto;
import cm.yowgifter.yow_gifter_backend.web.dto.UserPublicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<ConversationDto> getUserConversations(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Conversation> conversations = conversationRepository.findUserConversations(user.getId());

        List<ConversationDto> dtos = new ArrayList<>();
        for (Conversation c : conversations) {
            User contact = c.getDonor().getId().equals(user.getId()) ? c.getBeneficiary() : c.getDonor();
            List<Message> msgs = messageRepository.findByConversationIdOrderBySentAtAsc(c.getId());
            Message lastMsg = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);

            UserPublicDto contactPublic = UserPublicDto.builder()
                    .id(contact.getId())
                    .username(contact.getUsername())
                    .firstName(contact.getFirstName())
                    .city(contact.getCity())
                    .neighborhood(contact.getNeighborhood())
                    .avatarUrl(contact.getAvatarUrl())
                    .isVerified(contact.getIsVerified())
                    .build();

            dtos.add(ConversationDto.builder()
                    .id(c.getId())
                    .donationId(c.getDonation().getId())
                    .donationTitle(c.getDonation().getTitle())
                    .donationStatus(c.getDonation().getStatus().name())
                    .contact(contactPublic)
                    .lastMessage(lastMsg != null ? lastMsg.getContent() : "Conversation démarrée pour le rendez-vous.")
                    .lastMessageAt(lastMsg != null ? lastMsg.getSentAt() : c.getCreatedAt())
                    .createdAt(c.getCreatedAt())
                    .build());
        }
        return dtos;
    }

    public List<MessageDto> getMessages(Long conversationId, String userEmail) {
        Conversation conversation = getConversationAndVerifyAccess(conversationId, userEmail);
        User user = getUserByEmail(userEmail);

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId())
                .stream()
                .map(m -> MessageDto.builder()
                        .id(m.getId())
                        .conversationId(m.getConversation().getId())
                        .senderId(m.getSender().getId())
                        .senderName(m.getSender().getFirstName())
                        .content(m.getContent())
                        .sentAt(m.getSentAt())
                        .isMine(m.getSender().getId().equals(user.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendMessage(Long conversationId, String content, String userEmail) {
        if (content == null || content.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contenu du message ne peut pas être vide.");
        }

        Conversation conversation = getConversationAndVerifyAccess(conversationId, userEmail);
        User sender = getUserByEmail(userEmail);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content.trim())
                .build();

        message = messageRepository.save(message);

        return MessageDto.builder()
                .id(message.getId())
                .conversationId(conversation.getId())
                .senderId(sender.getId())
                .senderName(sender.getFirstName())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .isMine(true)
                .build();
    }

    private Conversation getConversationAndVerifyAccess(Long conversationId, String userEmail) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation introuvable."));

        User user = getUserByEmail(userEmail);

        boolean isParticipant = conversation.getDonor().getId().equals(user.getId()) ||
                                conversation.getBeneficiary().getId().equals(user.getId());

        if (!isParticipant) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès interdit à cette conversation privée.");
        }

        return conversation;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
    }
}

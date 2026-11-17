package cm.yowgifter.yow_gifter_backend.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    private Long id;
    private Long donationId;
    private String donationTitle;
    private String donationStatus;
    private UserPublicDto contact; // L'interlocuteur (donateur ou bénéficiaire)
    private String lastMessage;
    private Instant lastMessageAt;
    private Instant createdAt;
}

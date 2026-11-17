package cm.yowgifter.yow_gifter_backend.web.dto;

import cm.yowgifter.yow_gifter_backend.domain.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestDto {
    private Long id;
    private Long donationId;
    private String donationTitle;
    private Long beneficiaryId;
    private UserPublicDto beneficiary;
    private RequestStatus status;
    private Integer positionInQueue; // 1 = Prioritaire, 2 = Deuxième dans la file, etc.
    private Instant requestedAt;
    private Instant expiresAt;
    private Instant createdAt;
}

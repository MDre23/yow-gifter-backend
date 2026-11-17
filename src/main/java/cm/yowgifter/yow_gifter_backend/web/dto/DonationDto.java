package cm.yowgifter.yow_gifter_backend.web.dto;

import cm.yowgifter.yow_gifter_backend.domain.enums.DonationCondition;
import cm.yowgifter.yow_gifter_backend.domain.enums.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DonationDto {
    private Long id;
    private String title;
    private String description;
    private DonationCondition condition;
    private DonationStatus status;
    private String city;
    private String neighborhood;
    private Integer quantity;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private UserPublicDto donor;
    private List<String> imageUrls;
    private Integer waitingRequestsCount;
    private Instant createdAt;
    private Instant updatedAt;
}

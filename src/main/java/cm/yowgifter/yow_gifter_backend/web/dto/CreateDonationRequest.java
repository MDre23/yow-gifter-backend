package cm.yowgifter.yow_gifter_backend.web.dto;

import cm.yowgifter.yow_gifter_backend.domain.enums.DonationCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDonationRequest {

    @NotNull(message = "La catégorie est obligatoire")
    private Long categoryId;

    @NotBlank(message = "Le titre de l'objet est obligatoire")
    private String title;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    @NotNull(message = "L'état de l'objet est obligatoire")
    private DonationCondition condition;

    private String city;

    @NotBlank(message = "Le quartier de remise est obligatoire")
    private String neighborhood;

    @Builder.Default
    private Integer quantity = 1;
}

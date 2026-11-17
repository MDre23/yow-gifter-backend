package cm.yowgifter.yow_gifter_backend.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO Public conforme à REQ-F07 (Confidentialité citoyenne).
 * Les données sensibles (nom de famille, email exact, numéro de téléphone) ne sont JAMAIS exposées.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPublicDto {
    private Long id;
    private String username;
    private String firstName;
    private String city;
    private String neighborhood;
    private Integer age;
    private String bio;
    private String avatarUrl;
    private Boolean isVerified;
    private Instant createdAt;
}

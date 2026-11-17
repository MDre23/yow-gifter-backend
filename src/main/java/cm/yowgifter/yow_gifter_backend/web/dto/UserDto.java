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
public class UserDto {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String city;
    private String neighborhood;
    private Integer age;
    private String bio;
    private String avatarUrl;
    private Boolean isVerified;
    private Instant createdAt;
}

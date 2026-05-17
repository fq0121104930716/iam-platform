package iam.platform.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonRequest {
    @Size(max = 100)
    private String nickname;

    @Size(max = 500)
    private String avatarUrl;

    @Email(message = "Email format is invalid")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    private Boolean enabled;
}

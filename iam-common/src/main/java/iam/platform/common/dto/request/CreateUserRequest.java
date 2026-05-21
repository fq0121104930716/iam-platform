package iam.platform.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 100)
    private String nickname;

    @Size(max = 500)
    private String avatarUrl;

    /**
     * Initial password for user registration. This field is only used during self-registration flow
     * and will not be persisted directly. The password will be used to create a UserCredential
     * after user creation.
     * 
     * @deprecated Use separate credential creation API instead. This field is kept for registration
     *             flow compatibility.
     */
    @Deprecated
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;
}

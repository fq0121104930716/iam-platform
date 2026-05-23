package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.ChangePasswordRequest;
import iam.platform.common.dto.request.CreateUserCredentialRequest;
import iam.platform.common.dto.request.UpdateUserCredentialRequest;
import iam.platform.common.dto.response.UserCredentialResponse;
import iam.platform.admin.application.service.UserCredentialApplicationService;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

@RestController
@RequestMapping("/v1/users/{userId}/credentials")
@RequiredArgsConstructor
@Tag(name = "UserCredential", description = "User credential management API")
public class UserCredentialController {

    private final UserCredentialApplicationService credentialService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create credential for user")
    public ApiResponse<UserCredentialResponse> createCredential(@PathVariable Long userId,
            @Valid @RequestBody CreateUserCredentialRequest request) {
        return ApiResponse.created(credentialService.createCredential(userId, request));
    }

    @GetMapping("/{credentialId}")
    @Operation(summary = "Get credential by ID")
    public ApiResponse<UserCredentialResponse> getCredential(@PathVariable Long credentialId) {
        return ApiResponse.success(credentialService.getCredential(credentialId));
    }

    @GetMapping
    @Operation(summary = "List user credentials")
    public ApiResponse<PageResponse<UserCredentialResponse>> listCredentials(
            @PathVariable Long userId, @PageableDefault(size = 20) Pageable pageable) {
        var pageResult = credentialService.listCredentials(userId, pageable);
        return ApiResponse.success(PageResponse.of(pageResult.getContent(), pageResult.getNumber(),
                pageResult.getSize(), pageResult.getTotalElements()));
    }

    @PutMapping("/{credentialId}")
    @Operation(summary = "Update credential")
    public ApiResponse<UserCredentialResponse> updateCredential(@PathVariable Long userId,
            @PathVariable Long credentialId,
            @Valid @RequestBody UpdateUserCredentialRequest request) {
        return ApiResponse
                .success(credentialService.updateCredential(userId, credentialId, request));
    }

    @PutMapping("/{credentialId}/primary")
    @Operation(summary = "Set credential as primary")
    public ApiResponse<Void> setPrimary(@PathVariable Long userId,
            @PathVariable Long credentialId) {
        credentialService.setPrimary(userId, credentialId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{credentialId}/revoke")
    @Operation(summary = "Revoke credential")
    public ApiResponse<Void> revokeCredential(@PathVariable Long userId,
            @PathVariable Long credentialId) {
        credentialService.revokeCredential(userId, credentialId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{credentialId}")
    @Operation(summary = "Delete credential")
    public ApiResponse<Void> deleteCredential(@PathVariable Long userId,
            @PathVariable Long credentialId) {
        credentialService.deleteCredential(userId, credentialId);
        return ApiResponse.success(null);
    }

    @PutMapping("/password")
    @Operation(summary = "Change user password (shortcut)")
    public ApiResponse<Void> changePassword(@PathVariable Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        credentialService.changePassword(userId, request);
        return ApiResponse.success(null);
    }
}

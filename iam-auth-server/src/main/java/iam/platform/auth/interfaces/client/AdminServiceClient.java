package iam.platform.auth.interfaces.client;

import iam.platform.common.dto.request.CreateUserCredentialRequest;
import iam.platform.common.dto.request.CreateUserRequest;
import iam.platform.common.dto.response.UserCredentialResponse;
import iam.platform.auth.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenFeign client for admin service. Replaces the previous RestClient-based AdminApiClient.
 */
@FeignClient(name = "iam-admin-service", path = "/v1", configuration = FeignClientConfig.class)
public interface AdminServiceClient {

    /**
     * Create a new User via admin server API (used during self-registration).
     */
    @PostMapping("/persons")
    ResponseEntity<Void> createUser(@RequestBody CreateUserRequest request);

    /**
     * Create a credential for a user (used during self-registration to set initial password).
     */
    @PostMapping("/users/{userId}/credentials")
    ResponseEntity<UserCredentialResponse> createCredential(@PathVariable("userId") Long userId,
            @RequestBody CreateUserCredentialRequest request);
}

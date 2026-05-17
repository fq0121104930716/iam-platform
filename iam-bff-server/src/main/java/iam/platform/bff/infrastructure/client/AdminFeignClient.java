package iam.platform.bff.infrastructure.client;

import iam.platform.common.dto.request.CreatePersonRequest;
import iam.platform.bff.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenFeign client for admin service.
 */
@FeignClient(
    name = "iam-admin-service",
    path = "/v1",
    configuration = FeignClientConfig.class
)
public interface AdminFeignClient {

    /**
     * Create a new person via admin server API (used during self-registration).
     */
    @PostMapping("/persons")
    ResponseEntity<Void> createPerson(@RequestBody CreatePersonRequest request);
}

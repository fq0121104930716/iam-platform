package iam.platform.bff.infrastructure.client;

import iam.platform.bff.infrastructure.config.FeignClientConfig;
import iam.platform.common.dto.request.CreateUserRequest;
import iam.platform.common.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * OpenFeign client for admin service.
 */
@FeignClient(name = "iam-admin-service", path = "/v1", configuration = FeignClientConfig.class)
public interface AdminFeignClient {

    /**
     * Create a new User via admin service API (used during self-registration).
     */
    @PostMapping("/users")
    ResponseEntity<Void> createUser(@RequestBody CreateUserRequest request);

    /**
     * Get user details by user ID.
     */
    @GetMapping("/users/{userId}")
    ResponseEntity<UserResponse> getUser(@PathVariable String userId);

    /**
     * Get user's tenant mappings.
     */
    @GetMapping("/user-tenants/user/{userId}")
    ResponseEntity<List<Map<String, Object>>> getUserTenants(@PathVariable String userId);

    /**
     * Switch current tenant for user.
     */
    @PostMapping("/user-tenants/switch")
    ResponseEntity<Map<String, Object>> switchTenant(@RequestBody Map<String, String> request);

    /**
     * Get tenant's enabled platform menus.
     */
    @GetMapping("/menus/tenant/{tenantId}")
    ResponseEntity<List<Map<String, Object>>> getTenantMenus(@PathVariable Long tenantId);

    /**
     * Get tenant's assigned applications.
     */
    @GetMapping("/applications/tenant/{tenantId}")
    ResponseEntity<List<Map<String, Object>>> getTenantApplications(@PathVariable Long tenantId);

    /**
     * Get dashboard statistics.
     */
    @GetMapping("/admin/dashboard")
    ResponseEntity<Map<String, Object>> getDashboardStats();
}

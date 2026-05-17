package iam.platform.bff.application.service;

import iam.platform.bff.infrastructure.client.AdminFeignClient;
import iam.platform.common.dto.request.CreatePersonRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service for handling registration logic in BFF.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BffRegistrationService {

    private final AdminFeignClient adminFeignClient;

    /**
     * Register a new person via admin service.
     */
    public void registerPerson(CreatePersonRequest request) {
        log.info("Registering new person: {}", request.getEmail());
        ResponseEntity<Void> response = adminFeignClient.createPerson(request);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create person: " + response.getStatusCode());
        }
    }
}

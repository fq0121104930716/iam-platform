package iam.platform.admin.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.common.model.enums.TenantStatus;
import iam.platform.admin.domain.repository.TenantRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to automatically suspend expired tenants. This runs daily at
 * 2:00 AM to check for
 * tenants that have passed their expiration date.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TenantExpirationScheduler {

    private final TenantRepository tenantRepository;

    /**
     * Check for expired tenants and suspend them automatically. Runs daily at 2:00
     * AM.
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2:00 AM
    public void suspendExpiredTenants() {
        log.info("Starting scheduled task: suspend expired tenants");

        try {
            LocalDateTime now = LocalDateTime.now();

            // Get all active tenants
            List<Tenant> activeTenants = tenantRepository.findAllByStatus(TenantStatus.ACTIVE.name());

            int suspendedCount = 0;
            for (Tenant tenant : activeTenants) {
                // Check if tenant has expired
                if (tenant.getExpiresAt() != null && tenant.getExpiresAt().isBefore(now)) {
                    // Suspend the expired tenant via domain behavior
                    tenant.suspend();
                    tenantRepository.save(tenant);
                    suspendedCount++;
                    log.info("Auto-suspended expired tenant: {} (code: {}), expired at: {}",
                            tenant.getId(), tenant.getTenantCode(), tenant.getExpiresAt());
                }
            }

            log.info("Completed scheduled task: suspended {} expired tenants", suspendedCount);
        } catch (Exception e) {
            log.error("Error in scheduled task: suspend expired tenants", e);
        }
    }
}

package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.infrastructure.config.CasProperties;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing CAS (Central Authentication Service) tickets.
 * Handles Service Ticket (ST) generation and validation using Redis for distributed storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CasTicketService {

    private final StringRedisTemplate stringRedisTemplate;
    private final CasProperties casProperties;

    private static final String TICKET_KEY_PREFIX = "auth:cas:ticket:";

    /**
     * Create a new CAS Service Ticket.
     *
     * @param result the authentication result
     * @param service the service URL that requested the ticket
     * @return the generated Service Ticket string
     */
    public String createServiceTicket(AuthenticationResult result, String service) {
        String ticket = generateTicket(casProperties.getTicketPrefix());
        String key = TICKET_KEY_PREFIX + ticket;

        // Store ticket data in Redis with TTL
        String ticketData = String.format("%s|%s|%s|%s",
                result.person().getUsername(),
                result.person().getEmail() != null ? result.person().getEmail() : "",
                result.person().getNickname() != null ? result.person().getNickname() : "",
                service);

        try {
            stringRedisTemplate.opsForValue().set(key, ticketData,
                    Duration.ofSeconds(casProperties.getTicketValiditySeconds()));
            log.info("CAS Service Ticket created: {} for service: {}", ticket, service);
        } catch (Exception e) {
            log.error("Failed to store CAS ticket in Redis: {}", e.getMessage());
            // Fallback to in-memory storage
            fallbackStorage.put(key, ticketData);
        }

        return ticket;
    }

    /**
     * Validate a CAS Service Ticket and return user information.
     * The ticket is consumed (one-time use) after validation.
     *
     * @param ticket the Service Ticket to validate
     * @return CAS validation response, or null if ticket is invalid
     */
    public CasValidationResponse validateServiceTicket(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return null;
        }

        String key = TICKET_KEY_PREFIX + ticket;
        String ticketData;

        try {
            ticketData = stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis unavailable, checking fallback storage for ticket: {}", ticket);
            ticketData = fallbackStorage.remove(key);
        }

        if (ticketData == null || ticketData.isBlank()) {
            log.warn("CAS Service Ticket not found or already consumed: {}", ticket);
            return null;
        }

        // Consume the ticket (delete from Redis)
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to consume CAS ticket from Redis (already consumed): {}", ticket);
        }

        // Parse ticket data
        String[] parts = ticketData.split("\\|");
        if (parts.length < 3) {
            log.error("Invalid CAS ticket data format: {}", ticketData);
            return null;
        }

        return new CasValidationResponse(
                parts[0],  // username
                parts.length > 1 ? parts[1] : "",  // email
                parts.length > 2 ? parts[2] : ""   // nickname
        );
    }

    /**
     * Generate a CAS ticket string with the given prefix.
     */
    private String generateTicket(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "") + "-" +
                System.currentTimeMillis();
    }

    /**
     * In-memory fallback storage for tickets when Redis is unavailable.
     */
    private final Map<String, String> fallbackStorage = new ConcurrentHashMap<>();

    /**
     * CAS validation response containing user information.
     */
    public record CasValidationResponse(String username, String email, String nickname) {
    }
}

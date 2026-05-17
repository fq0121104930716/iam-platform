package iam.platform.auth.application.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import iam.platform.auth.infrastructure.config.SecurityPolicyProperties;

import java.net.InetAddress;
import java.util.List;

/**
 * IP whitelist/blacklist handler that blocks or allows authentication based on client IP.
 */
@Slf4j
@Component
public class IpWhitelistHandler implements PreAuthHandler {

    private final SecurityPolicyProperties properties;

    public IpWhitelistHandler(SecurityPolicyProperties properties) {
        this.properties = properties;
    }

    @Override
    public void handle(PreAuthContext context) {
        List<String> blacklist = properties.getIpBlacklist();
        List<String> whitelist = properties.getIpWhitelist();

        // If no lists configured, skip
        if (blacklist.isEmpty() && whitelist.isEmpty()) {
            return;
        }

        String clientIp = context.getClientIp();

        // Check blacklist first (always deny)
        if (isIpInList(clientIp, blacklist)) {
            log.warn("Blocked authentication attempt from blacklisted IP: {}", clientIp);
            throw new PreAuthenticationException("Access denied from your IP address");
        }

        // Check whitelist (if configured, only allow whitelisted IPs)
        if (!whitelist.isEmpty() && !isIpInList(clientIp, whitelist)) {
            log.warn("Blocked authentication attempt from non-whitelisted IP: {}", clientIp);
            throw new PreAuthenticationException("Access denied from your IP address");
        }
    }

    /**
     * Check if an IP address is in a list (supports CIDR notation).
     */
    private boolean isIpInList(String ip, List<String> list) {
        for (String entry : list) {
            if (entry.contains("/")) {
                // CIDR notation
                if (isInCidr(ip, entry)) {
                    return true;
                }
            } else {
                // Exact match
                if (ip.equals(entry)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if an IP is within a CIDR range.
     */
    private boolean isInCidr(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            String network = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            InetAddress address = InetAddress.getByName(ip);
            InetAddress networkAddress = InetAddress.getByName(network);

            byte[] addressBytes = address.getAddress();
            byte[] networkBytes = networkAddress.getAddress();

            // Compare bits up to prefix length
            for (int i = 0; i < prefixLength; i++) {
                int byteIndex = i / 8;
                int bitIndex = 7 - (i % 8);

                int addressBit = (addressBytes[byteIndex] >> bitIndex) & 1;
                int networkBit = (networkBytes[byteIndex] >> bitIndex) & 1;

                if (addressBit != networkBit) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("Failed to parse CIDR {}: {}", cidr, e.getMessage());
            return false;
        }
    }

    @Override
    public int getOrder() {
        return 40;
    }
}

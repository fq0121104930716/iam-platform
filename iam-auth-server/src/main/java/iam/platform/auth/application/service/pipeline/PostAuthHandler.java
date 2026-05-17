package iam.platform.auth.application.service.pipeline;

import org.springframework.core.Ordered;

/**
 * Interface for post-authentication pipeline handlers. Each handler performs one step in the
 * authentication completion process.
 */
public interface PostAuthHandler extends Ordered {
    void handle(PostAuthContext context);
}

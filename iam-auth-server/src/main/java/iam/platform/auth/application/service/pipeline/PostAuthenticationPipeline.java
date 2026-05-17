package iam.platform.auth.application.service.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;

import java.util.List;

/**
 * Orchestrator service that runs the chain of post-authentication handlers. Handlers are
 * auto-discovered via Spring DI and executed in order.
 */
@Service
@RequiredArgsConstructor
public class PostAuthenticationPipeline {

    private final List<PostAuthHandler> handlers;

    public AuthenticationResult execute(Person person, AuthenticationMethod method,
            HttpServletRequest request) {
        PostAuthContext ctx = new PostAuthContext(person, method, request);
        for (PostAuthHandler handler : handlers) {
            handler.handle(ctx);
        }
        return ctx.toResult();
    }
}

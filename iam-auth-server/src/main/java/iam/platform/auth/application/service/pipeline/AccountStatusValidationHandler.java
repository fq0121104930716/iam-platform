package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.service.AccountStatusPolicy;

/**
 * Pipeline handler: validates person and tenant account status before allowing authentication to
 * proceed.
 */
@Component
@RequiredArgsConstructor
public class AccountStatusValidationHandler implements PostAuthHandler {

    private final AccountStatusPolicy accountStatusPolicy;

    @Override
    public void handle(PostAuthContext context) {
        accountStatusPolicy.validatePersonStatus(context.getPerson());
    }

    @Override
    public int getOrder() {
        return 100;
    }
}

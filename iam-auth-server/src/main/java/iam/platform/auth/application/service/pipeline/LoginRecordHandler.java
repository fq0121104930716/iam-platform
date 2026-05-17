package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.repository.UserRepository;

/**
 * Pipeline handler: records the successful login timestamp.
 */
@Component
@RequiredArgsConstructor
public class LoginRecordHandler implements PostAuthHandler {

    private final UserRepository UserRepository;

    @Override
    public void handle(PostAuthContext context) {
        User user = context.getUser();
        user.recordLogin();
        UserRepository.save(user);
    }

    @Override
    public int getOrder() {
        return 200;
    }
}

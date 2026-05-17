package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.repository.PersonRepository;

/**
 * Pipeline handler: records the successful login timestamp.
 */
@Component
@RequiredArgsConstructor
public class LoginRecordHandler implements PostAuthHandler {

    private final PersonRepository personRepository;

    @Override
    public void handle(PostAuthContext context) {
        Person person = context.getPerson();
        person.recordLogin();
        personRepository.save(person);
    }

    @Override
    public int getOrder() {
        return 200;
    }
}

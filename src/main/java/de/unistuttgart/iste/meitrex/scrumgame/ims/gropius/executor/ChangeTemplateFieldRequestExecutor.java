package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class ChangeTemplateFieldRequestExecutor extends AbstractGropiusRequestExecutor<Optional<Issue>> {

    private final String issueId;
    private final String templateFieldName;
    private final Object newTemplateFieldValue;

    @Override
    protected Optional<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new ChangeIssueTemplatedFieldMutationRequest();
        request.setInput(GropiusChangeIssueTemplatedFieldInput.builder()
                .setIssue(issueId)
                .setName(templateFieldName)
                .setValue(newTemplateFieldValue)
                .build());

        var projection = new ChangeIssueTemplatedFieldPayloadResponseProjection()
                .templatedFieldChangedEvent(new TemplatedFieldChangedEventResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusChangeIssueTemplatedFieldPayload.class, projection)
                .retrieve()
                .flatMap(response -> response.getTemplatedFieldChangedEvent() == null
                        ? Mono.empty()
                        : Mono.just(gropiusIssueToDinoDevIssue(
                                response.getTemplatedFieldChangedEvent().getIssue(), mappingConfiguration)))
                .blockOptional();
    }
}

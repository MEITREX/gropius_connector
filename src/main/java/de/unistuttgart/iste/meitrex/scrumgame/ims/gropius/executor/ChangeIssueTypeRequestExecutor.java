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
public class ChangeIssueTypeRequestExecutor extends AbstractGropiusRequestExecutor<Optional<Issue>> {

    private final String issueId;
    private final String typeName;

    @Override
    protected Optional<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new ChangeIssueTypeMutationRequest();
        request.setInput(new GropiusChangeIssueTypeInput(issueId, typeName));

        var projection = new ChangeIssueTypePayloadResponseProjection()
                .typeChangedEvent(new TypeChangedEventResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusChangeIssueTypePayload.class, projection)
                .retrieve()
                .flatMap(response -> response.getTypeChangedEvent() == null
                        ? Mono.empty()
                        : Mono.just(gropiusIssueToDinoDevIssue(response.getTypeChangedEvent().getIssue(), mappingConfiguration)))
                .blockOptional();
    }
}

package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.generated.dto.IssueState;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class ChangeIssueStateRequestExecutor extends AbstractGropiusRequestExecutor<Optional<Issue>> {

    private final String issueId;
    private final IssueState issueState;

    @Override
    protected Optional<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new ChangeIssueStateMutationRequest();
        request.setInput(new GropiusChangeIssueStateInput(issueId, issueState.getImsStateId()));

        var projection = new ChangeIssueStatePayloadResponseProjection()
                .stateChangedEvent(new StateChangedEventResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusChangeIssueStatePayload.class, projection)
                .retrieve()
                .flatMap(response ->
                        response.getStateChangedEvent() == null
                                ? Mono.empty()
                                : Mono.just(gropiusIssueToDinoDevIssue(
                                response.getStateChangedEvent().getIssue(), mappingConfiguration)))
                .blockOptional();
    }
}

package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.generated.dto.IssuePriority;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class ChangeIssuePriorityRequestExecutor extends AbstractGropiusRequestExecutor<Optional<Issue>> {

    private final String issueId;
    private final IssuePriority priority;

    @Override
    protected Optional<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new ChangeIssuePriorityMutationRequest();
        request.setInput(new GropiusChangeIssuePriorityInput(issueId,
                mappingConfiguration.getIssuePriorityMapping().getIssuePriorityId(priority)));

        var projection = new ChangeIssuePriorityPayloadResponseProjection()
                .priorityChangedEvent(new PriorityChangedEventResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusChangeIssuePriorityPayload.class, projection)
                .retrieve()
                .flatMap(response -> {
                    if (response.getPriorityChangedEvent() == null) {
                        return Mono.empty();
                    }
                    return Mono.just(gropiusIssueToDinoDevIssue(response.getPriorityChangedEvent().getIssue(),
                            mappingConfiguration));
                })
                .blockOptional();
    }
}

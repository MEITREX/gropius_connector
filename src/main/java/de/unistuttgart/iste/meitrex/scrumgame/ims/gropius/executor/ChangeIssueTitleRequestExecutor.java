package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class ChangeIssueTitleRequestExecutor extends AbstractGropiusRequestExecutor<Issue> {

    private final String issueId;
    private final String newTitle;

    @Override
    protected Issue execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = ChangeIssueTitleMutationRequest.builder()
                .setInput(GropiusChangeIssueTitleInput.builder()
                        .setIssue(issueId)
                        .setTitle(newTitle)
                        .build())
                .build();

        var projection = new ChangeIssueTitlePayloadResponseProjection()
                .titleChangedEvent(new TitleChangedEventResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusChangeIssueTitlePayload.class, projection)
                .retrieve()
                .map(response -> gropiusIssueToDinoDevIssue(response.getTitleChangedEvent().getIssue(),
                        mappingConfiguration))
                .block();
    }
}

package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class ChangeIssueDescriptionRequestExecutor extends AbstractGropiusRequestExecutor<Issue> {

    private final String issueId;
    private final String description;

    @Override
    protected Issue execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new UpdateBodyMutationRequest();
        request.setInput(new GropiusUpdateBodyInput(description, issueId));

        var projection = new UpdateBodyPayloadResponseProjection()
                .body(new BodyResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusUpdateBodyPayload.class, projection)
                .retrieve()
                .map(response -> gropiusIssueToDinoDevIssue(response.getBody().getIssue(), mappingConfiguration))
                .block();
    }
}

package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class AssignIssueRequestExecutor extends AbstractGropiusRequestExecutor<Issue> {

    private final String issueId;
    private final UUID assigneeId;

    @Override
    protected Issue execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new CreateAssignmentMutationRequest();
        request.setInput(GropiusCreateAssignmentInput.builder()
                .setIssue(issueId)
                .setUser(assigneeId.toString())
                .build());

        var projection = new CreateAssignmentPayloadResponseProjection()
                .assignment(new AssignmentResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusCreateAssignmentPayload.class, projection)
                .retrieve()
                .map(response -> gropiusIssueToDinoDevIssue(response.getAssignment().getIssue(),
                        mappingConfiguration))
                .block();
    }
}

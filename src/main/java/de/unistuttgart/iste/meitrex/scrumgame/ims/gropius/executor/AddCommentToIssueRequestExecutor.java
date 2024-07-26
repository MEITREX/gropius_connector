package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class AddCommentToIssueRequestExecutor extends AbstractGropiusRequestExecutor<Issue>{

    private final String issueId;
    private final String comment;
    private final String optionalParentIssueId;

    @Override
    protected Issue execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new CreateIssueCommentMutationRequest();
        request.setInput(GropiusCreateIssueCommentInput.builder()
                .setIssue(issueId)
                .setBody(comment)
                .setAnswers(optionalParentIssueId)
                .build());

        var projection = new CreateIssueCommentPayloadResponseProjection()
                .issueComment(new IssueCommentResponseProjection()
                        .issue(GropiusProjections.getDefaultIssueProjection()));

        try {
            return graphQlRequestExecutor
                    .request(request)
                    .projectTo(GropiusCreateIssueCommentPayload.class, projection)
                    .retrieve()
                    .map(response -> gropiusIssueToDinoDevIssue(response.getIssueComment().getIssue(),
                            mappingConfiguration))
                    .block();
        } catch (Exception e) {
            // workaround: It seems like sometimes the parent issue is not found, so we try again without it
            request.setInput(GropiusCreateIssueCommentInput.builder()
                    .setIssue(issueId)
                    .setBody(comment)
                    .build());

            return graphQlRequestExecutor
                    .request(request)
                    .projectTo(GropiusCreateIssueCommentPayload.class, projection)
                    .retrieve()
                    .map(response -> gropiusIssueToDinoDevIssue(response.getIssueComment().getIssue(),
                            mappingConfiguration))
                    .block();
        }
    }
}

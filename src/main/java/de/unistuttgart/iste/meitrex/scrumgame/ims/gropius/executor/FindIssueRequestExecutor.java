package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.GropiusIssue;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class FindIssueRequestExecutor extends AbstractGropiusRequestExecutor<Optional<Issue>> {

    private final String issueId;

    @Override
    protected Optional<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor,
                                      GropiusIssueMappingConfiguration mappingConfiguration) {
        return graphQlRequestExecutor
                .request(GropiusRequests.getIssueQueryRequest(issueId))
                .projectTo(GropiusIssue.class, GropiusProjections.getDefaultIssueProjection())
                .retrieveList()
                .map(issues -> GropiusMapping.gropiusIssueToDinoDevIssue(issues.getFirst(), mappingConfiguration))
                .blockOptional();
    }
}

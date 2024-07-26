package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.GropiusProjectConnection;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.getIssuesFromProjectAndComponents;
import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class GetIssuesRequestExecutor extends AbstractGropiusRequestExecutor<List<Issue>> {

    @Override
    protected List<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor,
                                  GropiusIssueMappingConfiguration mappingConfiguration) {
        return graphQlRequestExecutor
                .request(GropiusRequests.getProjectRequest(mappingConfiguration.getImsProjectId()))
                .projectTo(GropiusProjectConnection.class, GropiusProjections.getProjectConnectionProjection())
                .retrieve()
                .map(response -> getAllIssuesFromProjectConnection(response, mappingConfiguration))
                .defaultIfEmpty(List.of())
                .block();
    }

    private List<Issue> getAllIssuesFromProjectConnection(GropiusProjectConnection response,
                                                          GropiusIssueMappingConfiguration mappingConfiguration) {
        return getIssuesFromProjectAndComponents(response)
                .map(gropiusIssue -> gropiusIssueToDinoDevIssue(gropiusIssue, mappingConfiguration))
                .filter(Objects::nonNull)
                .toList();
    }

}

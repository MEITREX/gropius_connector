package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.GropiusIssue;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.CreateEventInput;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
public class GetEventsOfIssueRequestExecutor extends AbstractGropiusRequestExecutor<List<CreateEventInput>> {

    private final String issueId;
    private final OffsetDateTime since;

    @Override
    protected List<CreateEventInput> execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var projection = GropiusProjections.getIssueWithTimelineItemsProjection(since);

        return graphQlRequestExecutor
                .request(GropiusRequests.getIssueQueryRequest(issueId))
                .projectTo(GropiusIssue.class, projection)
                .retrieveList()
                .map(issues -> getTimeLineItemsOfFirstIssue(mappingConfiguration, issues))
                .defaultIfEmpty(List.of())
                .block();
    }

    static List<CreateEventInput> getTimeLineItemsOfFirstIssue(GropiusIssueMappingConfiguration mappingConfiguration,
                                                               List<GropiusIssue> issues) {
        if (issues.isEmpty()) {
            return List.of();
        }

        var issue = issues.getFirst();
        return GropiusMapping.getTimeLineItemsOfIssue(mappingConfiguration, issue);
    }
}

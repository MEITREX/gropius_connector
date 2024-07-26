package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.CreateEventInput;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.getIssuesFromProjectAndComponents;

@RequiredArgsConstructor
public class GetEventsForProjectRequestExecutor extends AbstractGropiusRequestExecutor<List<CreateEventInput>> {

    private final OffsetDateTime since;

    @Override
    protected List<CreateEventInput> execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = GropiusRequests.getProjectRequest(mappingConfiguration.getImsProjectId());

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusProjectConnection.class, getProjection())
                .retrieve()
                .map(response -> getIssuesFromProjectAndComponents(response)
                        .flatMap(gropiusIssue -> GropiusMapping.getTimeLineItemsOfIssue(mappingConfiguration, gropiusIssue).stream())
                        .toList())
                .block();
    }

    private ProjectConnectionResponseProjection getProjection() {
        return new ProjectConnectionResponseProjection()
                .nodes(new ProjectResponseProjection()
                        .issues(new ProjectIssuesParametrizedInput()
                                        .filter(GropiusIssueFilterInput.builder()
                                                .setLastModifiedAt(GropiusDateTimeFilterInput.builder()
                                                        .setGt(since.toString())
                                                        .build())
                                                .build()),
                                new IssueConnectionResponseProjection()
                                        .nodes(GropiusProjections.getIssueWithTimelineItemsProjection(since)))
                        .components(new ComponentVersionConnectionResponseProjection()
                                .nodes(new ComponentVersionResponseProjection()
                                        .component(new ComponentResponseProjection()
                                                .issues(new IssueConnectionResponseProjection()
                                                        .nodes(GropiusProjections.getIssueWithTimelineItemsProjection(since)))))));
    }


}

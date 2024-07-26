package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.CreateIssueInput;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping.gropiusIssueToDinoDevIssue;

@RequiredArgsConstructor
public class CreateIssueRequestExecutor extends AbstractGropiusRequestExecutor<Issue> {

    private final CreateIssueInput createIssueInput;

    @Override
    protected Issue execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = new CreateIssueMutationRequest();
        request.setInput(GropiusCreateIssueInput.builder()
                .setTrackables(List.of(mappingConfiguration.getImsProjectId()))
                .setTitle(createIssueInput.getTitle())
                .setBody(createIssueInput.getDescription())
                .setState(mappingConfiguration.getIssueStateConverter()
                        .getIssueStateId(createIssueInput.getStateName()))
                .setType(mappingConfiguration.getIssueTypeMapping().getIssueTypeId(createIssueInput.getTypeName()))
                .setTemplate(mappingConfiguration.getIssueTemplateId())
                .setTemplatedFields(List.of(
                        GropiusJSONFieldInput.builder()
                                .setName(mappingConfiguration.getSprintFieldName())
                                .setValue(createIssueInput.getSprintNumber())
                                .build()))
                .build());

        var projection = new CreateIssuePayloadResponseProjection()
                .issue(GropiusProjections.getDefaultIssueProjection());

        return graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusCreateIssuePayload.class, projection)
                .retrieve()
                .map(response -> gropiusIssueToDinoDevIssue(response.getIssue(), mappingConfiguration))
                .block();
    }
}

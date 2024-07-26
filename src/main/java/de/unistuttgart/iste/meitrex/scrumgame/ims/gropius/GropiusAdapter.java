package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius;

import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.scrumgame.ims.ImsAdapter;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class GropiusAdapter implements ImsAdapter {

    private final GraphQlRequestExecutor           graphQlRequestExecutor;
    private final GropiusIssueMappingConfiguration mappingConfiguration;

    @Override
    public List<Issue> getIssues(UUID dinodevProjectId) {
        return new GetIssuesRequestExecutor().executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public Optional<Issue> findIssue(String id) {
        return new FindIssueRequestExecutor(id).executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public List<Issue> findIssuesBatched(List<String> ids) {
        return new FindIssuesBatchedRequestExecutor(ids).executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public Issue changeIssueTitle(String issueId, String title) {
        return new ChangeIssueTitleRequestExecutor(issueId, title)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public Issue changeIssueDescription(String issueId, String description) {
        return new ChangeIssueDescriptionRequestExecutor(issueId, description)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public Issue changeIssueState(String issueId, IssueState issueState) {
        return new ChangeIssueStateRequestExecutor(issueId, issueState)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration)
                .orElseGet(() -> findIssue(issueId).orElseThrow());
    }

    @Override
    public Issue changeIssuePriority(String issueId, IssuePriority priority) {
        return new ChangeIssuePriorityRequestExecutor(issueId, priority)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration)
                .orElseGet(() -> findIssue(issueId).orElseThrow());
    }

    @Override
    public Issue changeIssueType(String issueId, String typeName) {
        return new ChangeIssueTypeRequestExecutor(issueId, typeName)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration)
                .orElseGet(() -> findIssue(issueId).orElseThrow());
    }

    @Override
    public Issue changeSprintOfIssue(String issueId, @Nullable Integer sprintNumber) {
        return changeTemplateField(issueId, mappingConfiguration.getSprintFieldName(), sprintNumber);
    }

    @Override
    public Issue changeEstimationOfIssue(String issueId, TShirtSizeEstimation estimation) {
        return changeTemplateField(issueId,
                mappingConfiguration.getEstimationTemplateFieldName(),
                estimation.toString());
    }

    private Issue changeTemplateField(
            String issueId,
            String fieldName,
            Object value) {

       return new ChangeTemplateFieldRequestExecutor(issueId, fieldName, value)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration)
                .orElseGet(() -> findIssue(issueId).orElseThrow());
    }

    @Override
    public Issue assignIssue(String issueId, UUID assigneeId) {
        return new AssignIssueRequestExecutor(issueId, assigneeId)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public Issue addCommentToIssue(String issueId, String comment, @Nullable String optionalParentIssueId) {
        return new AddCommentToIssueRequestExecutor(issueId, comment, optionalParentIssueId)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public Issue createIssue(CreateIssueInput createIssueInput) {
        return new CreateIssueRequestExecutor(createIssueInput)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public List<CreateEventInput> getEventsForIssue(String issueId,
            OffsetDateTime since) {
        return new GetEventsOfIssueRequestExecutor(issueId, since)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }

    @Override
    public List<CreateEventInput> getEventsForProject(UUID projectId, OffsetDateTime since) {
        return new GetEventsForProjectRequestExecutor(since)
                .executeRequest(graphQlRequestExecutor, mappingConfiguration);
    }
}

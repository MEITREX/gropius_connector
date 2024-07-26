package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping;

import de.unistuttgart.iste.gropius.generated.dto.*;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor.GropiusProjections.TrackableResponse;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.util.TShirtSizeEstimationStoryPointsConverter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static de.unistuttgart.iste.meitrex.common.util.MeitrexCollectionUtils.distinctByKey;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class GropiusMapping {

    public static Issue gropiusIssueToDinoDevIssue(
            GropiusIssue issueResponse,
            GropiusIssueMappingConfiguration mappingConfiguration) {

        if (issueResponse == null || issueResponse.getTemplate() == null
            || !issueResponse.getTemplate().getId().equals(mappingConfiguration.getIssueTemplateId())) {
            return null; // TODO make this method return optional
        }

        Optional<Object> estimationField = findTemplateField(
                issueResponse.getTemplatedFields(),
                mappingConfiguration.getEstimationTemplateFieldName());
        Optional<TShirtSizeEstimation> estimation = estimationField
                .map(Object::toString)
                .flatMap(estimateString -> parseEnum(estimateString, TShirtSizeEstimation.class));
        Optional<Integer> storyPointsValue = estimation
                .map(TShirtSizeEstimationStoryPointsConverter::getStoryPoints);

        return Issue.builder()
                .setId(issueResponse.getId())
                .setProjectId(mappingConfiguration.getDinoDevProjectId())
                .setTitle(issueResponse.getTitle())
                .setDescription(issueResponse.getBody().getBody())
                .setState(mappingConfiguration.getIssueStateConverter()
                        .getIssueState(issueResponse.getState().getId()))
                .setType(IssueType.builder()
                        .setName(issueResponse.getType().getName())
                        .setDescription(issueResponse.getType().getDescription())
                        .setIconPath(issueResponse.getType().getIconPath())
                        .build())
                .setPriority(getIssuePriority(issueResponse.getPriority(), mappingConfiguration))
                .setAssigneeIds(
                        issueResponse.getAssignments().getNodes().stream()
                                .map(assignment -> assignment.getUser().getId())
                                .map(UUID::fromString)
                                .toList())
                .setSprintNumber(
                        findTemplateField(issueResponse.getTemplatedFields(), mappingConfiguration.getSprintFieldName())
                                .flatMap(GropiusMapping::parseIntSafely)
                                .orElse(null))
                .setStoryPoints(storyPointsValue.orElse(null))
                .setEffortEstimation(estimation.orElse(null))
                .setLabels(issueResponse.getLabels().getNodes().stream()
                        .map(GropiusLabel::getName)
                        .toList())
                .setIssueUrl(getIssueUrl(issueResponse, mappingConfiguration))
                .build();
    }

    public static Stream<GropiusIssue> getIssuesFromProjectAndComponents(GropiusProjectConnection response) {
        Stream<GropiusIssue> issuesFromProject = response.getNodes().stream()
                .flatMap(project -> project.getIssues().getNodes().stream());
        Stream<GropiusIssue> issuesFromComponents = response.getNodes().stream()
                .flatMap(project -> project.getComponents().getNodes().stream())
                .flatMap(component -> component.getComponent().getIssues().getNodes().stream());

        return Stream.concat(issuesFromProject, issuesFromComponents)
                .filter(distinctByKey(GropiusIssue::getId)); // remove duplicates
    }

    private static String getIssueUrl(GropiusIssue issueResponse,
            GropiusIssueMappingConfiguration mappingConfiguration) {
        List<TrackableResponse> trackables = issueResponse.getTrackables().getNodes();
        Optional<TrackableResponse> firstTrackable = trackables.stream().findFirst();

        String issueUrl = mappingConfiguration.getGropiusBaseUrl();
        if (firstTrackable.isPresent()) {
            if ("Project".equals(firstTrackable.get().typename())) {
                issueUrl += "/projects/" + firstTrackable.get().id();
            } else if ("Component".equals(firstTrackable.get().typename())) {
                issueUrl += "/components/" + firstTrackable.get().id();
            }
        } else {
            issueUrl += "/projects/" + mappingConfiguration.getImsProjectId();
        }
        issueUrl += "/issues/" + issueResponse.getId();
        return issueUrl;
    }

    private static IssuePriority getIssuePriority(GropiusIssuePriority priority,
            GropiusIssueMappingConfiguration mappingConfiguration) {
        IssuePriority issuePriority = null;
        if (priority != null) {
            issuePriority = mappingConfiguration.getIssuePriorityMapping().getIssuePriority(priority.getId());
        }
        return issuePriority != null ? issuePriority : IssuePriority.MEDIUM;
    }

    private static Optional<Object> findTemplateField(List<GropiusJSONField> templatedFields, String fieldName) {
        return templatedFields.stream()
                .filter(field -> field.getName().equals(fieldName))
                .map(GropiusJSONField::getValue)
                .filter(Objects::nonNull)
                .findFirst();
    }

    private static <T extends Enum<T>> Optional<T> parseEnum(String value, Class<T> enumType) {
        try {
            return Optional.of(Enum.valueOf(enumType, value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Optional<Integer> parseIntSafely(Object value) {
        if (value instanceof Integer intValue) {
            return Optional.of(intValue);
        }

        if (value instanceof String stringValue) {
            try {
                return Optional.of(Integer.parseInt(stringValue));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public static List<CreateEventInput> getTimeLineItemsOfIssue(GropiusIssueMappingConfiguration mappingConfiguration,
                                                                 GropiusIssue issue) {
        return issue.getTimelineItems().getNodes().stream()
                .flatMap(timelineItem -> GropiusTimelineItemToEventConverter
                        .convertTimelineItemToEvents(issue, timelineItem, mappingConfiguration).stream())
                .toList();
    }
}

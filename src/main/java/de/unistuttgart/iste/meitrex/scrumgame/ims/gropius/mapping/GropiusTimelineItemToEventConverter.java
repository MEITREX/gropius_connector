package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping;

import de.unistuttgart.iste.gropius.generated.dto.GropiusIssue;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.scrumgame.ims.ImsEventTypes;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor.GropiusProjections.TimelineItemResponse;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import de.unistuttgart.iste.meitrex.scrumgame.util.StateUtils;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts a Gropius timeline item to a DinoDev event.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class GropiusTimelineItemToEventConverter {

    public static List<CreateEventInput> convertTimelineItemToEvents(
            GropiusIssue issue,
            TimelineItemResponse timelineItem,
            GropiusIssueMappingConfiguration configuration
    ) {
        EventType eventType = getEventType(timelineItem, configuration);

        CreateEventInput baseEvent = CreateEventInput.builder()
                .setId(timelineItem.id())
                .setTimestamp(timelineItem.createdAt())
                .setUserId(UUID.fromString(timelineItem.lastModifiedBy().getId()))
                .setProjectId(configuration.getDinoDevProjectId())
                .setEventTypeIdentifier(eventType.getIdentifier())
                .build();

        if (baseEvent.getEventTypeIdentifier().equals(ImsEventTypes.ISSUE_UPDATED.getIdentifier())) {
            baseEvent.setMessage(getMessage(timelineItem, configuration));
        }
        if (baseEvent.getEventTypeIdentifier().equals(ImsEventTypes.COMMENT_ON_ISSUE.getIdentifier())) {
            baseEvent.setMessage(timelineItem.body());
        }

        if (timelineItem.answers() != null) {
            baseEvent.setParentId(UUID.fromString(timelineItem.answers().getId()));
        }

        baseEvent.setEventData(getEventData(issue, timelineItem, eventType));

        if (baseEvent.getEventTypeIdentifier().equals(ImsEventTypes.ISSUE_COMPLETED.getIdentifier())
            && !issue.getAssignments().getNodes().isEmpty()) {
                return issue.getAssignments().getNodes().stream()
                        .map(assignment -> CreateEventInput.builder()
                                .setEventData(baseEvent.getEventData())
                                .setEventTypeIdentifier(baseEvent.getEventTypeIdentifier())
                                // unique ID for each event, also prevents opening and closing the same issue multiple times
                                .setId(UUID.nameUUIDFromBytes((issue.getId() +
                                                               assignment.getUser().getId()).getBytes()))
                                .setMessage(baseEvent.getMessage())
                                .setParentId(baseEvent.getParentId())
                                .setProjectId(baseEvent.getProjectId())
                                .setTimestamp(baseEvent.getTimestamp())
                                .setUserId(UUID.fromString(assignment.getUser().getId()))
                                .build())
                        .toList();
            }


        return List.of(baseEvent);
    }

    private static EventType getEventType(
            TimelineItemResponse timelineItem,
            GropiusIssueMappingConfiguration configuration
    ) {
        return switch (timelineItem.typename()) {
            case "IssueComment", "Comment" -> ImsEventTypes.COMMENT_ON_ISSUE;
            case "Body" -> ImsEventTypes.ISSUE_CREATED; // Body is the first timeline item of an issue
            case "Assignment" -> ImsEventTypes.ASSIGNED_ISSUE;
            case "RemovedAssignmentEvent" -> ImsEventTypes.UNASSIGNED_ISSUE;
            case "StateChangedEvent" -> getStateChangedEventType(timelineItem, configuration);
            default -> ImsEventTypes.ISSUE_UPDATED; // more specific event types can be implemented as needed
        };
    }

    private static EventType getStateChangedEventType(TimelineItemResponse timelineItem,
            GropiusIssueMappingConfiguration configuration) {
        IssueState oldState = configuration.getIssueStateConverter().getIssueState(timelineItem.oldState().getId());
        IssueState newState = configuration.getIssueStateConverter().getIssueState(timelineItem.newState().getId());

        if (StateUtils.isMovedToDone(oldState, newState)) {
            return ImsEventTypes.ISSUE_COMPLETED;
        }
        if (StateUtils.isMovedOutOfSprint(oldState, newState)) {
            return ImsEventTypes.REMOVE_ISSUE_FROM_SPRINT;
        }
        if (StateUtils.isMovedIntoSprint(oldState, newState)) {
            return ImsEventTypes.ADD_ISSUE_TO_SPRINT;
        }
        if (StateUtils.isReopened(oldState, newState)) {
            return ImsEventTypes.ISSUE_REOPENED;
        }
        if (StateUtils.isMovedToInProgress(oldState, newState)) {
            return ImsEventTypes.START_PROGRESS;
        }

        return ImsEventTypes.ISSUE_UPDATED;
    }

    private static String getMessage(
            TimelineItemResponse timelineItemResponse,
            GropiusIssueMappingConfiguration configuration
    ) {
        return switch (timelineItemResponse.typename()) {
            case "AddedLabelEvent" -> "added the label " + timelineItemResponse.addedLabel().getName() + ".";
            case "PriorityChangedEvent" -> getPriorityChangedMessage(timelineItemResponse);
            case "TemplatedFieldChangedEvent" -> "changed the field " + timelineItemResponse.fieldName() + " from "
                                                 + timelineItemResponse.oldValue() + " to " +
                                                 timelineItemResponse.newValue() + ".";
            case "RemovedTemplatedFieldEvent" -> "removed the field " + timelineItemResponse.fieldName() + ".";
            case "TitleChangedEvent" -> "changed the title from " + timelineItemResponse.oldTitle() + " to "
                                        + timelineItemResponse.newTitle() + ".";
            case "TypeChangedEvent" -> getTypeChangedMessage(timelineItemResponse);
            case "RemovedLabelEvent" -> "removed the label " + timelineItemResponse.removedLabel().getName() + ".";
            case "StateChangedEvent" -> getStateChangedMessage(timelineItemResponse, configuration);

            default -> "updated the issue, see details in Gropius."; // add more cases as needed
        };
    }

    private static String getStateChangedMessage(
            TimelineItemResponse timelineItemResponse,
            GropiusIssueMappingConfiguration configuration
    ) {
        IssueState oldState = configuration.getIssueStateConverter()
                .getIssueState(timelineItemResponse.oldState().getId());
        IssueState newState = configuration.getIssueStateConverter()
                .getIssueState(timelineItemResponse.newState().getId());

        return "changed the state from " + oldState.getName() + " to " + newState.getName() + ".";
    }

    private static String getPriorityChangedMessage(TimelineItemResponse timelineItemResponse) {
        if (timelineItemResponse.oldPriority() != null && timelineItemResponse.newPriority() != null) {
            return "changed the priority from " + timelineItemResponse.oldPriority().getValue()
                   + " to " + timelineItemResponse.newPriority().getValue() + ".";
        }
        if (timelineItemResponse.newPriority() != null) {
            return "changed the priority to " + timelineItemResponse.newPriority().getValue() + ".";
        }

        return "changed the priority.";
    }

    private static String getTypeChangedMessage(TimelineItemResponse timelineItemResponse) {
        if (timelineItemResponse.oldType() != null && timelineItemResponse.newType() != null) {
            return "changed the type from " + timelineItemResponse.oldType().getName() + " to "
                   + timelineItemResponse.newType().getName() + ".";
        }
        if (timelineItemResponse.newType() != null) {
            return "changed the type to " + timelineItemResponse.newType().getName() + ".";
        }
        return "changed the issue type.";
    }

    private static List<DataFieldInput> getEventData(
            GropiusIssue issue,
            TimelineItemResponse timelineItem,
            EventType eventType
    ) {
        var eventData = new ArrayList<DataFieldInput>();

        eventData.add(new DataFieldInput("issueId", AllowedDataType.STRING, issue.getId()));
        eventData.add(new DataFieldInput("issueTitle", AllowedDataType.STRING, issue.getTitle()));
        eventData.add(new DataFieldInput("assigneeIds", AllowedDataType.STRING,
                issue.getAssignments().getNodes().stream()
                        .map(assignment -> assignment.getUser().getId())
                        .collect(Collectors.joining(","))));
        eventData.add(new DataFieldInput("assigneeNames", AllowedDataType.STRING,
                issue.getAssignments().getNodes().stream()
                        .map(assignment -> assignment.getUser().getUsername())
                        .collect(Collectors.joining(","))));

        if (eventType == ImsEventTypes.COMMENT_ON_ISSUE) {
            eventData.add(new DataFieldInput("comment", AllowedDataType.STRING, timelineItem.body()));
        }
        if (eventType == ImsEventTypes.ASSIGNED_ISSUE || eventType == ImsEventTypes.UNASSIGNED_ISSUE) {
            if (timelineItem.user() != null) {
                var userId = UUID.fromString(timelineItem.user().getId());
                eventData.add(new DataFieldInput("assigneeId", AllowedDataType.STRING, userId.toString()));
                eventData.add(new DataFieldInput("assigneeName",
                        AllowedDataType.STRING,
                        timelineItem.user().getUsername()));
            }
        }

        return eventData;
    }
}

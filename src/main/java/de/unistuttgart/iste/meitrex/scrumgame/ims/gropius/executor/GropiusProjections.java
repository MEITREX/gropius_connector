package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Some commonly used projections for Gropius responses.
 */
@SuppressWarnings("OverlyCoupledClass")
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class GropiusProjections {

    private static final IssueResponseProjection DEFAULT_ISSUE_RESPONSE_PROJECTION = new IssueResponseProjection()
            .id()
            .trackables(new TrackableConnectionResponseProjection()
                    .nodes(new TrackableResponseProjection().id().typename()))
            .title()
            .template(new IssueTemplateResponseProjection().id())
            .body(new BodyResponseProjection().body())
            .state(new IssueStateResponseProjection().id())
            .priority(new IssuePriorityResponseProjection().id().value())
            .type(new IssueTypeResponseProjection()
                    .id()
                    .name()
                    .description()
                    .iconPath())
            .assignments(new AssignmentConnectionResponseProjection()
                    .nodes(new AssignmentResponseProjection()
                            .id()
                            .user(new UserResponseProjection().id().username())))
            .labels(new LabelConnectionResponseProjection()
                    .nodes(new LabelResponseProjection().id().name()))
            .templatedFields(new JSONFieldResponseProjection().all$());

    public static final TimelineItemConnectionResponseProjection TIMELINE_ITEM_CONNECTION_RESPONSE_PROJECTION
            = new TimelineItemConnectionResponseProjection()
            .nodes(new TimelineItemResponseProjection()
                    .id()
                    .typename()
                    .createdAt()
                    .lastModifiedAt()
                    .lastModifiedBy(new UserResponseProjection().id())
                    .onAddedLabelEvent(new AddedLabelEventResponseProjection()
                            .addedLabel(new LabelResponseProjection().id().name()))
                    .onAssignment(new AssignmentResponseProjection()
                            .user(new UserResponseProjection().username().id()))
                    .onIssueComment(new IssueCommentResponseProjection()
                            .body()
                            .answers(new CommentResponseProjection().id()))
                    .onPriorityChangedEvent(new PriorityChangedEventResponseProjection()
                            .newPriority(new IssuePriorityResponseProjection().id().value())
                            .oldPriority(new IssuePriorityResponseProjection().id().value()))
                    .onRemovedAssignmentEvent(new RemovedAssignmentEventResponseProjection()
                            .removedAssignment(new AssignmentResponseProjection().user(new UserResponseProjection().username()
                                    .id())))
                    .onStateChangedEvent(new StateChangedEventResponseProjection()
                            .newState(new IssueStateResponseProjection().id())
                            .oldState(new IssueStateResponseProjection().id()))
                    .onTemplatedFieldChangedEvent(new TemplatedFieldChangedEventResponseProjection()
                            .newValue()
                            .oldValue()
                            .fieldName())
                    .onRemovedTemplatedFieldEvent(new RemovedTemplatedFieldEventResponseProjection()
                            .fieldName())
                    .onRemovedLabelEvent(new RemovedLabelEventResponseProjection()
                            .removedLabel(new LabelResponseProjection().id().name()))
                    .onTitleChangedEvent(new TitleChangedEventResponseProjection()
                            .newTitle()
                            .oldTitle())
                    .onTypeChangedEvent(new TypeChangedEventResponseProjection()
                            .newType(new IssueTypeResponseProjection().id().name())
                            .oldType(new IssueTypeResponseProjection().id().name())
                    ));

    public static IssueResponseProjection getIssueWithTimelineItemsProjection(OffsetDateTime since) {
        return new IssueResponseProjection()
                .id()
                .title()
                .assignments(new AssignmentConnectionResponseProjection()
                        .nodes(new AssignmentResponseProjection()
                                .id()
                                .user(new UserResponseProjection().id().username())))
                .timelineItems(new IssueTimelineItemsParametrizedInput()
                                .filter(GropiusTimelineItemFilterInput.builder()
                                        .setLastModifiedAt(GropiusDateTimeFilterInput.builder()
                                                .setGt(since.toString())
                                                .build())
                                        .build())
                                .orderBy(List.of(GropiusTimelineItemOrder.builder()
                                        .setField(GropiusTimelineItemOrderField.CREATED_AT)
                                        .setDirection(GropiusOrderDirection.DESC)
                                        .build()))
                                .first(1000),
                        TIMELINE_ITEM_CONNECTION_RESPONSE_PROJECTION);
    }

    // record to serialize the response of the timeline query
    @SuppressWarnings("ClassWithTooManyFields")
    public record TimelineItemResponse(
            UUID id,
            String __typename,
            OffsetDateTime createdAt,
            OffsetDateTime lastModifiedAt,
            GropiusGropiusUser lastModifiedBy,
            GropiusLabel addedLabel,
            GropiusGropiusUser user,
            String body,
            GropiusIssueComment answers,
            GropiusIssuePriority newPriority,
            GropiusIssuePriority oldPriority,
            GropiusAssignment removedAssignment,
            GropiusIssueState newState,
            GropiusIssueState oldState,
            String newValue,
            String oldValue,
            String fieldName,
            GropiusLabel removedLabel,
            String newTitle,
            String oldTitle,
            GropiusIssueType newType,
            GropiusIssueType oldType
    ) {

        public String typename() {
            return __typename;
        }
    }

    public record TrackableResponse(UUID id, String __typename) {

        public String typename() {
            return __typename;
        }
    }

    public static ProjectConnectionResponseProjection getProjectConnectionProjection() {
        return new ProjectConnectionResponseProjection()
                .nodes(new ProjectResponseProjection()
                        .issues(getDefaultIssueConnectionProjection())
                        .components(new ComponentVersionConnectionResponseProjection()
                                .nodes(new ComponentVersionResponseProjection()
                                        .component(new ComponentResponseProjection()
                                                .issues(getDefaultIssueConnectionProjection())))));
    }

    public static IssueConnectionResponseProjection getDefaultIssueConnectionProjection() {
        return new IssueConnectionResponseProjection().nodes(getDefaultIssueProjection());
    }

    public static IssueResponseProjection getDefaultIssueProjection() {
        return DEFAULT_ISSUE_RESPONSE_PROJECTION;
    }

}

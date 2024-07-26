package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Commonly used Gropius requests.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GropiusRequests {

    public static ProjectsQueryRequest getProjectRequest(String imsProjectId) {
        return ProjectsQueryRequest.builder()
                .setFilter(GropiusProjectFilterInput.builder()
                        .setId(idFilter(imsProjectId))
                        .build())
                .build();
    }

    public static SearchIssuesQueryRequest getIssueQueryRequest(String issueId) {
        return SearchIssuesQueryRequest.builder()
                .setQuery("*") // query must be given
                .setFirst(1)
                .setFilter(GropiusIssueFilterInput.builder()
                        .setId(idFilter(issueId))
                        .build())
                .build();
    }

    static GropiusIDFilterInput idFilter(String id) {
        return GropiusIDFilterInput.builder().setEq(id).build();
    }

}

package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.gropius.generated.dto.GropiusIDFilterInput;
import de.unistuttgart.iste.gropius.generated.dto.GropiusIssue;
import de.unistuttgart.iste.gropius.generated.dto.GropiusIssueFilterInput;
import de.unistuttgart.iste.gropius.generated.dto.SearchIssuesQueryRequest;
import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.common.util.MeitrexCollectionUtils;
import de.unistuttgart.iste.meitrex.generated.dto.Issue;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.mapping.GropiusMapping;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FindIssuesBatchedRequestExecutor extends AbstractGropiusRequestExecutor<List<Issue>> {

    private final List<String> issueIds;

    @Override
    protected List<Issue> execute(GraphQlRequestExecutor graphQlRequestExecutor,
                                  GropiusIssueMappingConfiguration mappingConfiguration) {
        var request = SearchIssuesQueryRequest.builder()
                .setQuery("*") // query must be given
                .setFirst(issueIds.size())
                .setFilter(GropiusIssueFilterInput.builder()
                        .setId(GropiusIDFilterInput.builder().setIn(issueIds).build())
                        .build())
                .build();

        List<Issue> issues = graphQlRequestExecutor
                .request(request)
                .projectTo(GropiusIssue.class, GropiusProjections.getDefaultIssueProjection())
                .retrieveList()
                .map(gropiusIssues -> gropiusIssues.stream()
                        .map(gropiusIssue -> GropiusMapping.gropiusIssueToDinoDevIssue(gropiusIssue,
                                mappingConfiguration))
                        .toList())
                .blockOptional()
                .orElseThrow();

        return MeitrexCollectionUtils.sortByKeys(issues, issueIds, Issue::getId);
    }
}

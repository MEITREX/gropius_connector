package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps issue type names to their corresponding IMS issue type IDs.
 */
public class IssueTypeMapping {

    private final Map<String, String> issueNameToImsIssueIdMap;

    public IssueTypeMapping(List<IssueTypeConfiguration> issueTypes) {
        issueNameToImsIssueIdMap = issueTypes.stream()
                .collect(Collectors.toMap(
                        IssueTypeConfiguration::name,
                        IssueTypeConfiguration::imsTypeId));
    }

    public String getIssueTypeId(String issueTypeName) {
        return issueNameToImsIssueIdMap.get(issueTypeName);
    }

    public record IssueTypeConfiguration(String imsTypeId, String name) {
    }

}

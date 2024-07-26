package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config;

import de.unistuttgart.iste.meitrex.generated.dto.IssuePriority;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Maps issue priorities between the DinoDev and the IMS.
 */
@RequiredArgsConstructor
public class IssuePriorityMapping {

    private final Map<String, IssuePriority> issuePriorityMap;


    public IssuePriority getIssuePriority(String imsPriorityId) {
        return issuePriorityMap.get(imsPriorityId);
    }

    public String getIssuePriorityId(IssuePriority issuePriority) {
        return issuePriorityMap.entrySet().stream()
                .filter(entry -> entry.getValue() == issuePriority)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(issuePriority.toString());
    }
}

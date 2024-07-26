package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config;

import java.util.UUID;

/**
 * Configuration for the issue mapping between DinoDev and the IMS.
 */
public interface GropiusIssueMappingConfiguration {

    /**
     * Get the UUID of the DinoDev project.
     *
     * @return The DinoDev project ID.
     */
    UUID getDinoDevProjectId();

    /**
     * Get the ID of the project in the IMS.
     *
     * @return The IMS project ID.
     */
    String getImsProjectId();

    /**
     * Get the issue state converter for this configuration, used to convert issue states between DinoDev and the
     * IMS.
     *
     * @return The issue state converter.
     */
    IssueStateMapping getIssueStateConverter();

    /**
     * Get the issue priority mapping for this configuration, used to map issue priorities between DinoDev and
     * the IMS.
     *
     * @return The issue priority mapping.
     */
    IssuePriorityMapping getIssuePriorityMapping();

    /**
     * Get the issue type mapping for this configuration, used to map issue types between DinoDev and the IMS.
     */
    IssueTypeMapping getIssueTypeMapping();

    /**
     * Get the name of the custom field in the IMS that contains the sprint information.
     */
    String getSprintFieldName();

    /**
     * Get the name of the custom field in the IMS that contains the estimation information.
     */
    String getEstimationTemplateFieldName();

    /**
     * Get the ID of the issue template in the IMS.
     */
    String getIssueTemplateId();

    /**
     * Get the base URL of the Gropius instance.
     */
    String getGropiusBaseUrl();
}

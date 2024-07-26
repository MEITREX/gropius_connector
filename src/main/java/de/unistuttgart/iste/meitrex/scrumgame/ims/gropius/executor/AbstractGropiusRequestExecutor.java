package de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.executor;

import de.unistuttgart.iste.meitrex.common.graphqlclient.GraphQlRequestExecutor;
import de.unistuttgart.iste.meitrex.scrumgame.ims.gropius.config.GropiusIssueMappingConfiguration;

import org.springframework.security.access.AccessDeniedException;

public abstract class AbstractGropiusRequestExecutor<T> {

    protected abstract T execute(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration);

    public T executeRequest(GraphQlRequestExecutor graphQlRequestExecutor, GropiusIssueMappingConfiguration mappingConfiguration) {
        try {
            return execute(graphQlRequestExecutor, mappingConfiguration);
        } catch (Exception e) {
            // workaround for "Invalid JWT" error
            if (e.getMessage().toLowerCase().contains("invalid jwt")) {
                throw new AccessDeniedException("Invalid JWT");
            }
            throw e;
        }
    }
}

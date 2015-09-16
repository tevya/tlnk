package org.tevya.repo;

import org.springframework.stereotype.Component;
import org.tevya.model.LinkDefinition;

/**
 * Definition of the LinkDefinitionRepository API
 */
@Component
public interface LinkDefinitionRepository {
    void initialize() throws Exception;
    boolean aliasExists(String alias);
    boolean keyExists(String key);
    boolean add(LinkDefinition linkDefinition) throws Exception;
    boolean deleteByKey(String key);
    boolean deleteByAlias(String alias);
    LinkDefinition getByKey(String key);
    LinkDefinition getByAlias(String alias);
    void deleteAll();
}

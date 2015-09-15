package org.tevya.org.tevya.repo;

import org.springframework.stereotype.Component;
import org.tevya.org.tevya.model.LinkDefinition;

/**
 * Created by Eric on 9/13/2015.
 */
@Component
public interface LinkDefinitionRepository {
    public void initialize() throws Exception;
    public boolean aliasExists(String alias);
    public boolean keyExists(String key);
    public boolean save(LinkDefinition linkDefinition) throws Exception;
    public boolean deleteByKey(String key);
    public boolean deleteByAlias(String alias);
    public LinkDefinition getByKey(String key);
    public LinkDefinition getByAlias(String alias);
    public void deleteAll();
}

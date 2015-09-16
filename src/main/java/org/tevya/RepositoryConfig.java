package org.tevya;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tevya.repo.LinkDefinitionFlatFileRepository;
import org.tevya.repo.LinkDefinitionRepository;

/**
 * Created by Eric on 9/14/2015.
 */
@Configuration
public class RepositoryConfig {

    @Bean
    public LinkDefinitionRepository  getRepository() throws Exception {
        return new LinkDefinitionFlatFileRepository();
    }

    @Bean
    public LinkGenerator getLinkGenerator() {
        return new LinkGenerator();
    }
}

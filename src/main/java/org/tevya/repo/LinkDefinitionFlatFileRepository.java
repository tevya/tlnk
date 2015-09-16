package org.tevya.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.tevya.model.LinkDefinition;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Flat file implementation of a LinkDefinitionRepository
 */
public class LinkDefinitionFlatFileRepository implements LinkDefinitionRepository{

    /**
     * Constructor for unit testing.
     * @param testContents  JSON-formatted link definitions.
     */
    public LinkDefinitionFlatFileRepository(String testContents) {
        this.testContents = testContents;
        this.backingFilePath = null;
    }

    public static class LinkDefinitions {
        ArrayList<LinkDefinition>  definitions;

        public LinkDefinitions() {
            definitions = new ArrayList<LinkDefinition>();
        }

        public void setDefinitions(ArrayList<LinkDefinition> newDefinitions) {
            this.definitions = newDefinitions;
        }

        public ArrayList<LinkDefinition> getDefinitions() {
            return this.definitions;
        }
    }

    private Path  backingFilePath;
    private String testContents;

    static private Logger logger = Logger.getLogger(LinkDefinitionFlatFileRepository.class.getName());

    private Map<String,LinkDefinition> knownKeyLinks = new HashMap<String, LinkDefinition>();
    private Map<String,LinkDefinition> knownAliasLinks = new HashMap<String, LinkDefinition>();

    private LinkDefinitions currentLinkDefinitions;

    public LinkDefinitionFlatFileRepository() throws Exception {
        //TODO: Check that TLNK_DATA is defined.
        String dataDirectory = System.getenv("TLNK_DATA");
        if (dataDirectory == null || !Files.isDirectory(Paths.get(dataDirectory)))
        {
            throw new Exception("TLNK_DATA must be defined to indicate the location of tinylink_map.json");
        }
        backingFilePath = Paths.get(String.format("%s/tinylink_map.json", dataDirectory));
    }

    private void ingestDefinitions(LinkDefinitions defSet) {
        for (LinkDefinition ld : defSet.definitions){
            if (StringUtils.isNoneBlank(ld.getAlias())){
                knownAliasLinks.put(ld.getAlias(),ld);
            } else {
                knownKeyLinks.put(ld.getKey(),ld);
            }
        }
    }

    public synchronized void initialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        if (StringUtils.isNotEmpty(this.testContents)) {
            currentLinkDefinitions = mapper.readValue(this.testContents, LinkDefinitions.class);
            ingestDefinitions(currentLinkDefinitions);
            return;
        }

        if (Files.notExists(backingFilePath)){
            // create and initialize the file with an empty set of link definitions.
            Path actualPath = Files.createFile(backingFilePath);
            currentLinkDefinitions = new LinkDefinitions();
            ingestDefinitions(currentLinkDefinitions);
            mapper.writeValue(new File(actualPath.toString()), currentLinkDefinitions);
            return;
        }

        try {
            currentLinkDefinitions = mapper.readValue(new File(backingFilePath.toString()), LinkDefinitions.class);
            ingestDefinitions(currentLinkDefinitions);
        } catch (IOException e) {
            String message = String.format("Cannot read %s: Exception is %s", backingFilePath, e);
            logger.severe(message);
            if (currentLinkDefinitions == null ) {
                currentLinkDefinitions = new LinkDefinitions();
            }
        }
    }

    public boolean aliasExists(String alias) {
        return knownAliasLinks.containsKey(alias.toLowerCase());
    }

    public boolean keyExists(String key) {
        return knownKeyLinks.containsKey(key);
    }

    public synchronized boolean add(LinkDefinition linkDefinition) throws Exception {
        if (linkDefinition.getAlias() != null && aliasExists(linkDefinition.getAlias()))
        {
            return false; // duplicate alias.
        }
        if (keyExists(linkDefinition.getKey())) {
            logger.warning(String.format("Duplicate key %s found.", linkDefinition.getKey()));
            return false;
        }
        currentLinkDefinitions.definitions.add(linkDefinition);
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (StringUtils.isNotEmpty(this.testContents)) {
                this.testContents = mapper.writeValueAsString(currentLinkDefinitions);
            }
            else {
                mapper.writeValue(new File(backingFilePath.toString()), currentLinkDefinitions);
            }
            initialize();
            return true;
        } catch (IOException e) {
            logger.severe(String.format("Cannot add LinkDefinition for %s.  Exception: %s", linkDefinition.getTargetUrl(), e));
            currentLinkDefinitions.definitions.remove(currentLinkDefinitions.definitions.size());
            return false;
        }
    }

    public boolean deleteByKey(String key){
        if (keyExists(key))
        {
            synchronized (this) {
                for (LinkDefinition ld : currentLinkDefinitions.definitions) {
                    if (ld.getKey().equals(key)) {
                        currentLinkDefinitions.definitions.remove(ld);
                        break;
                    }
                }
                try {
                    initialize();
                } catch (Exception e) {
                    logger.warning(String.format("Reinitialization failed: %s", e));
                }
            }
            return true;
        }
        return false;
    }

    public boolean deleteByAlias(String alias) {
        if (aliasExists(alias))
        {
            synchronized (this) {
                for (LinkDefinition ld : currentLinkDefinitions.definitions) {
                    if (ld.getAlias().equals(alias)) {
                        currentLinkDefinitions.definitions.remove(ld);
                        break;
                    }
                }
                try {
                    initialize();
                } catch (Exception e) {
                    logger.warning(String.format("Reinitialization failed: %s", e));
                }
            }
            return true;
        }
        return false;
    }

    public LinkDefinition getByKey(String key) {
        return keyExists(key) ? knownKeyLinks.get(key) : null;
    }

    public LinkDefinition getByAlias(String alias) {
        return aliasExists(alias) ? knownAliasLinks.get(alias) : null;
    }

    public void deleteAll() {
        if (Files.exists(backingFilePath)) {
            logger.info(String.format("Deleting backing file %s", backingFilePath));
            try {
                Files.delete(backingFilePath);
            } catch (IOException e) {
                logger.warning(String.format("Cannot delete %s", backingFilePath));
            }
            try {
                initialize();
            } catch (Exception e) {
                logger.warning("Problem reinitializing.");
            }
        }
    }
}

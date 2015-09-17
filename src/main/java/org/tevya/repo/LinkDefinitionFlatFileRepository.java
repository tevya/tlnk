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
     * This inner class exists primarily for serializing and deserializing link definitions.
     */
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

    /**
     * The location of the backing file.
     */
    private Path  backingFilePath;

    static private Logger logger = Logger.getLogger(LinkDefinitionFlatFileRepository.class.getName());

    // Organized into maps for faster lookup.
    private Map<String,LinkDefinition> knownKeyLinks = new HashMap<String, LinkDefinition>();
    private Map<String,LinkDefinition> knownAliasLinks = new HashMap<String, LinkDefinition>();

    public LinkDefinitionFlatFileRepository() throws Exception {
        String dataDirectory = System.getenv("TLNK_DATA");
        if (dataDirectory == null || !Files.isDirectory(Paths.get(dataDirectory)))
        {
            throw new Exception("TLNK_DATA must be defined to indicate the location of tinylink_map.json");
        }
        backingFilePath = Paths.get(String.format("%s/tinylink_map.json", dataDirectory));
    }

    private void ingestDefinitions(LinkDefinitions defSet) {
        knownAliasLinks.clear();
        knownKeyLinks.clear();
        for (LinkDefinition ld : defSet.definitions){
            if (StringUtils.isNotEmpty(ld.getAlias())){
                knownAliasLinks.put(ld.getAlias(),ld);
            } else {
                knownKeyLinks.put(ld.getKey(),ld);
            }
        }
    }

    /**
     * Initialize from the disk file.
     *
     * @throws Exception
     */
    public synchronized void initialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        LinkDefinitions linkSet;
        if (Files.notExists(backingFilePath)){
            // create and initialize the file with an empty set of link definitions.
            Path actualPath = Files.createFile(backingFilePath);
            linkSet = new LinkDefinitions();
            ingestDefinitions(linkSet);
            mapper.writeValue(new File(actualPath.toString()), linkSet);
            return;
        }

        try {
            linkSet = mapper.readValue(new File(backingFilePath.toString()), LinkDefinitions.class);
            ingestDefinitions(linkSet);
        } catch (IOException e) {
            String message = String.format("Cannot read %s: Exception is %s", backingFilePath, e);
            logger.severe(message);
        }
    }

    private boolean update() {
        // Merge together the link definitions
        LinkDefinitions localDefs = new LinkDefinitions();
        ArrayList<LinkDefinition> definitionList = new ArrayList<LinkDefinition>();
        definitionList.addAll(knownKeyLinks.values());
        definitionList.addAll(knownAliasLinks.values());
        localDefs.setDefinitions(definitionList);

        // and write them out.
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(backingFilePath.toString()), localDefs);
        } catch (Exception e) {
            logger.warning(String.format("Repository update failed: %s", e));
            return false;
        }
        return true;
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

        // this should never happen
        if (keyExists(linkDefinition.getKey())) {
            logger.warning(String.format("Duplicate key %s found.", linkDefinition.getKey()));
            return false;
        }
        if (StringUtils.isNotEmpty(linkDefinition.getAlias())) {
            knownAliasLinks.put(linkDefinition.getAlias(),linkDefinition);
        }
        else {
            knownKeyLinks.put(linkDefinition.getKey(),linkDefinition);
        }

        boolean result = update();
        if (!result)
        {
            logger.severe(String.format("Cannot add LinkDefinition for %s.", linkDefinition.getTargetUrl()));
        }
        return result;
    }

    public boolean deleteByKey(String key){
        if (keyExists(key))
        {
            synchronized (this) {
                knownKeyLinks.remove(key);
                return update();
            }
        }
        return false;
    }

    public boolean deleteByAlias(String alias) {
        if (aliasExists(alias))
        {
            synchronized (this) {
                knownAliasLinks.remove(alias);
                return update();
            }
        }
        return false;
    }

    public LinkDefinition getByKey(String key) {
        return knownKeyLinks.get(key);
    }

    public LinkDefinition getByAlias(String alias) {
        return knownAliasLinks.get(alias);
    }

    public synchronized void deleteAll() {
        knownAliasLinks.clear();
        knownKeyLinks.clear();
        update();
    }
}

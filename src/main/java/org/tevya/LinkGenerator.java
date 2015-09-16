package org.tevya;


import org.springframework.stereotype.Component;
import org.tevya.repo.LinkDefinitionRepository;

import java.util.Random;
import java.util.logging.Logger;

/**
 * Holder of logic to create new key values
 */
@Component
public class LinkGenerator {

    private static final int minimum = 1;
    private static final int baseValue = 36; // use largest practical base to minimize key length in URL.

    /**
     * The maximum is initialized low and only expanded as collisions
     * indicate necessary.  This keeps early key values "tiny" without
     * limiting the overall key domain.
     */
    private static int maximum = Integer.parseInt("100", baseValue);

    Logger logger = Logger.getLogger(LinkGenerator.class.getName());

    // Usually this can be a field rather than a method variable
    private static final Random rand = new Random();

    /**
     * Returns a key that does not exist in the repository.
     * The key returned is random so that users cannot predict others' mappings.
     * @param repo  the repository to check against.
     * @return alias key value
     * @throws Exception if cannot find a unique key (after ~3 billion !)
     */
    public String getNewKey(LinkDefinitionRepository repo) throws Exception {
        String candidate = generateRandomKey();
        while(maximum < Integer.MAX_VALUE/2) {
            for (int i = 0; i < 3; i++) {
                if (!repo.keyExists(candidate)) {
                    return candidate;
                }
            }
            maximum = 2*maximum;  // expand the size of the key name space.
        }
        // We have run out of values.  This should never happen.
        // Now go through the values until we find a gap.
        for (int key = 1; key < maximum; key++)
        {
            candidate = Integer.toString(key, baseValue);
            if (!repo.keyExists(candidate)) {
                return candidate;
            }
        }
        logger.severe("Completely exhausted address space for links");
        throw new Exception("Completely exhausted address space.");
    }

    private String generateRandomKey() {
        int randomNum = rand.nextInt(maximum) + minimum;
        return Integer.toString(randomNum, baseValue);
    }

}

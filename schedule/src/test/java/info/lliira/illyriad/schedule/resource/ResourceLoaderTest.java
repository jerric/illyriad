package info.lliira.illyriad.schedule.resource;


import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class ResourceLoaderTest
{
    private Authenticator authenticator;

    @BeforeEach
    public void setUp() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
        authenticator = new Authenticator(properties);
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void loadTowns()
    {
        var loader = new ResourceLoader(authenticator);
        var towns = loader.loadTowns();
        assertFalse(towns.towns.isEmpty());
        assertTrue(towns.current().isPresent());
        assertEquals(8, towns.resources.size());
        for (var type : Resource.Type.values()) {
            if (type != Resource.Type.Mana) {
                var resource = towns.resources.get(type);
                assertTrue(resource.amount > 0);
                assertTrue(resource.rate > 0);
            }
        }
    }
}

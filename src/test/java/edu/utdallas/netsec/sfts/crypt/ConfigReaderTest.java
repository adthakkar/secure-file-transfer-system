package edu.utdallas.netsec.sfts.crypt;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Properties;

/**
 * @author Fahad Shaon
 */
public class ConfigReaderTest {

    @Test
    public void testReadConfig() throws Exception {
        Properties properties = ConfigReader.readConfig("data/test/config/client.properties");

        Assert.assertEquals(properties.getProperty("as.hostname"), "127.0.0.1");
        Assert.assertEquals(properties.getProperty("as.port"), "27011");

        Assert.assertEquals(properties.getProperty("emfs.hostname"), "127.0.0.1");
        Assert.assertEquals(properties.getProperty("emfs.port"), "27017");
    }
}

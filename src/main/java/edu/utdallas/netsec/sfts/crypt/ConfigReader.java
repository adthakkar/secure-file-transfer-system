package edu.utdallas.netsec.sfts.crypt;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Fahad Shaon
 */
public class ConfigReader {

    public static Properties readConfig(String filename) throws IOException {

        Properties properties = new Properties();
        properties.load(new FileInputStream(filename));

        return properties;
    }

}

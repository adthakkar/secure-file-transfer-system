package edu.utdallas.netsec.sfts;

import edu.utdallas.netsec.sfts.crypt.ConfigReader;
import edu.utdallas.netsec.sfts.crypt.KeyManager;
import edu.utdallas.netsec.sfts.crypt.KeyPair;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Class for generating pre-shared keys among AS and other entities.
 *
 * @author Fahad Shaon
 */
public class Initializer {

    public static void printUsages() {
        System.out.println("Usages: ./run.sh init keys /path/to/as.properties");
    }

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.err.println("Invalid arguments.");
            printUsages();
            return;
        }

        if ("keys".equals(args[0])) {
            String filename = args[1];

            Properties asProperties;

            try {
                asProperties = ConfigReader.readConfig(filename);

            } catch (IOException e) {
                System.err.println("Please provide a valid path for Authentication Server Properties");
                return;
            }

            for (String s : asProperties.stringPropertyNames()) {

                if (s.endsWith(".key")) {

                    String keyFilename = asProperties.getProperty(s);
                    System.out.println("Generating key: " + s + ", Saving to: " + keyFilename);

                    File key = new File(keyFilename);
                    File parentFile = key.getParentFile();
                    if (!parentFile.exists()) {
                        if (parentFile.mkdirs()) {
                            System.out.println("Directory didn't exist created " + parentFile.getAbsolutePath());
                        } else {
                            System.out.println("Unable to create directory. Please make sure the path is writable.");
                        }
                    }

                    KeyPair keyPair = KeyManager.generateKeyPair();
                    KeyManager.saveKeyPairToFile(keyPair, keyFilename);
                }
            }
            return;
        }

        System.out.println("Invalid arguments.");
        printUsages();
    }
}

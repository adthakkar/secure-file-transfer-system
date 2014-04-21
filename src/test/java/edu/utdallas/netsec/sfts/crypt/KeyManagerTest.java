package edu.utdallas.netsec.sfts.crypt;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.security.Key;

/**
 * @author Fahad Shaon
 */
public class KeyManagerTest {

    @Test
    public void testKeyPairReadWrite() throws Exception {

        String filename = "data/test/keys/randkey-2.key";
        File keyFile = new File(filename);

        Assert.assertFalse(keyFile.exists());

        KeyPair keyPair = KeyManager.generateKeyPair();
        KeyManager.saveKeyPairToFile(keyPair, filename);

        Assert.assertTrue(keyFile.exists());

        KeyPair keyPairRead = KeyManager.readKeyPairFromFile(filename);

        Assert.assertEquals(keyPair.getKeyBytes(), keyPairRead.getKeyBytes());

        if (!keyFile.delete()) {
            System.err.println("Temporary file (" + filename + ") created for testing wasn't deleted");
        }

    }
}

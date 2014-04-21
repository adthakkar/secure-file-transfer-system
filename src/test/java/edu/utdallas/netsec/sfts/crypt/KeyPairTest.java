package edu.utdallas.netsec.sfts.crypt;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.SecureRandom;

/**
 * @author Fahad Shaon
 */
public class KeyPairTest {

    @Test
    public void testKeyPair() throws Exception {
        byte[] keyBytes = new byte[64];
        new SecureRandom().nextBytes(keyBytes);

        KeyPair keyPair = new KeyPair(keyBytes);

        byte[] secBytes = new byte[32];
        byte[] authBytes = new byte[32];

        System.arraycopy(keyBytes, 0, secBytes, 0, 32);
        System.arraycopy(keyBytes, 32, authBytes, 0, 32);

        Assert.assertEquals(keyPair.getKeyBytes(), keyBytes);
        Assert.assertEquals(keyPair.getSecretKey().getEncoded(), secBytes);
        Assert.assertEquals(keyPair.getAuthenticationKey().getEncoded(), authBytes);
    }
}

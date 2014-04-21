package edu.utdallas.netsec.sfts.crypt;

import java.security.SecureRandom;

/**
 * @author Fahad Shaon
 */
public class RandomUtils {

    public static final int NONCE_LENGTH = 8;
    private static SecureRandom secureRandom = new SecureRandom();

    public static byte[] getSecureRandomBytes(int length) {

        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);

        return bytes;
    }

    public static byte[] getNonce() {
        return getSecureRandomBytes(NONCE_LENGTH);
    }

}

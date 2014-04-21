package edu.utdallas.netsec.sfts.crypt;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * @author Fahad Shaon
 */
public class KeyPair {

    public static final String MAC_ALGORITHM = "HmacSHA256";
    public static final String ENCRYPTION_ALGORITHM = "AES";

    private SecretKeySpec authenticationKey;
    private SecretKeySpec secretKey;
    private byte[] keyBytes;


    public KeyPair(byte[] keyBytes) {

        if (keyBytes == null) {
            throw new RuntimeException("Must provide keyBytes.");
        }

        if (keyBytes.length != 64) {
            throw new RuntimeException("Invalid number of bytes Need exactly 64 bytes (512 bits).");
        }

        this.keyBytes = keyBytes;
        byte[] secretKeyBytes = new byte[32];
        byte[] authKeyBytes = new byte[32];

        System.arraycopy(keyBytes, 0, secretKeyBytes, 0, 32);
        System.arraycopy(keyBytes, 32, authKeyBytes, 0, 32);

        secretKey = new SecretKeySpec(secretKeyBytes, ENCRYPTION_ALGORITHM);
        authenticationKey = new SecretKeySpec(authKeyBytes, MAC_ALGORITHM);
    }

    public Key getAuthenticationKey() {
        return authenticationKey;
    }

    public Key getSecretKey() {
        return secretKey;
    }

    public byte[] getKeyBytes() {
        return keyBytes;
    }
}

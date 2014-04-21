package edu.utdallas.netsec.sfts.crypt;

import java.io.IOException;

/**
 * @author Fahad Shaon
 */
public class KeyManager {

    public static void saveKeyPairToFile(KeyPair keyPair, String filename) throws IOException {
        FileUtils.writeFileBinary(filename, keyPair.getKeyBytes());
    }

    public static KeyPair readKeyPairFromFile(String filename) throws IOException {
        byte[] keyBytes = FileUtils.readFileBinary(filename);
        return new KeyPair(keyBytes);
    }

    public static KeyPair generateKeyPair() {
        return new KeyPair(RandomUtils.getSecureRandomBytes(64));
    }
    
    public static KeyPair generateKeyPair(byte[] keyBytes) {
        return new KeyPair(keyBytes);
    }
}

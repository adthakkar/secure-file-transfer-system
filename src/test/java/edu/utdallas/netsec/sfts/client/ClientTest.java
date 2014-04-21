package edu.utdallas.netsec.sfts.client;

import edu.utdallas.netsec.sfts.crypt.KeyManager;
import edu.utdallas.netsec.sfts.crypt.KeyPair;
import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import edu.utdallas.netsec.sfts.packets.ClientRequest;
import edu.utdallas.netsec.sfts.packets.EncryptedPayload;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * @author Fahad Shaon
 */
public class ClientTest {

    public static final String CIPHER_ALGORITHM_PRP = "AES/CBC/PKCS5Padding";
    public static final String MAC_ALGORITHM = "HmacSHA256";

    @Test
    public void testCryptAlgorithms() throws Exception {
        //Don't write test following this. it is suppose to be practice code for practicing crypto-library

        ClientRequest clientRequest = new ClientRequest();

        clientRequest.clientName = "Test Client";
        clientRequest.departServer = "finance";
        clientRequest.nonce = RandomUtils.getSecureRandomBytes(8);

        byte[] encoded = clientRequest.encode();
        byte[] iv = RandomUtils.getSecureRandomBytes(16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        KeyPair keyPair = KeyManager.generateKeyPair();

        EncryptedPayload encryptedPayload = new EncryptedPayload();

        Cipher aesCipher = Cipher.getInstance(CIPHER_ALGORITHM_PRP);
        aesCipher.init(Cipher.ENCRYPT_MODE, keyPair.getSecretKey(), ivParameterSpec);
        byte[] cipherText = aesCipher.doFinal(encoded);

        encryptedPayload.iv = iv;
        encryptedPayload.payload = cipherText;

        Mac sha256Mac = Mac.getInstance(MAC_ALGORITHM);
        sha256Mac.init(keyPair.getAuthenticationKey());

        encryptedPayload.mac = sha256Mac.doFinal(encoded);

        ivParameterSpec = new IvParameterSpec(encryptedPayload.iv);

        Cipher aesCipherDecrypt = Cipher.getInstance(CIPHER_ALGORITHM_PRP);
        aesCipherDecrypt.init(Cipher.DECRYPT_MODE, keyPair.getSecretKey(), ivParameterSpec);

        byte[] decrypted = aesCipherDecrypt.doFinal(encryptedPayload.payload);

        Mac sha256MacDecrypt = Mac.getInstance(MAC_ALGORITHM);
        sha256MacDecrypt.init(keyPair.getAuthenticationKey());

        byte[] decryptedMac = sha256MacDecrypt.doFinal(decrypted);

        Assert.assertEquals(decryptedMac, encryptedPayload.mac);

        ClientRequest clientRequestDecrypted = new ClientRequest();
        clientRequestDecrypted.decode(decrypted);

        Assert.assertEquals(clientRequestDecrypted.clientName, clientRequest.clientName);
        Assert.assertEquals(clientRequestDecrypted.departServer, clientRequest.departServer);
        Assert.assertEquals(clientRequestDecrypted.nonce, clientRequest.nonce);
    }
}

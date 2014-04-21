package edu.utdallas.netsec.sfts.packets;

import edu.utdallas.netsec.sfts.crypt.KeyPair;
import edu.utdallas.netsec.sfts.crypt.RandomUtils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public abstract class Packet implements Serializable {

    public static final String CIPHER_ALGORITHM_PRP = "AES/CBC/PKCS5Padding";
    public static final String MAC_ALGORITHM = "HmacSHA256";

    abstract public byte[] encode();

    abstract public void decode(byte[] encoded);

    abstract public byte getPacketId();

    abstract public byte getFieldCount();

    public void checkHeader(byte[] encoded) {

        int fieldCount = encoded[0];
        if (getFieldCount() != fieldCount) {
            throw new InvalidPacket("Field Count doesn't match");
        }

        //todo add more checking on header..
    }

    public EncryptedPayload encrypt(KeyPair keyPair) {

        EncryptedPayload encryptedPayload = new EncryptedPayload();
        encryptedPayload.packetId = getPacketId();

        byte[] iv = RandomUtils.getSecureRandomBytes(16);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        byte[] encoded = encode();

        try {
            Cipher aesCipher = Cipher.getInstance(CIPHER_ALGORITHM_PRP);
            aesCipher.init(Cipher.ENCRYPT_MODE, keyPair.getSecretKey(), ivParameterSpec);
            byte[] cipherText = aesCipher.doFinal(encoded);

            encryptedPayload.iv = iv;
            encryptedPayload.payload = cipherText;

            Mac sha256Mac = Mac.getInstance(MAC_ALGORITHM);
            sha256Mac.init(keyPair.getAuthenticationKey());

            encryptedPayload.mac = sha256Mac.doFinal(encoded);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return encryptedPayload;
    }

    public void decrypt(KeyPair keyPair, EncryptedPayload encryptedPayload) {

        if (encryptedPayload.packetId != getPacketId()) {
            throw new InvalidPacket("Packet Id doesn't match. Id in packet is  " + encryptedPayload.packetId
                    + ", where  packet id should be " + getPacketId());
        }

        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(encryptedPayload.iv);

            Cipher aesCipherDecrypt = Cipher.getInstance(CIPHER_ALGORITHM_PRP);
            aesCipherDecrypt.init(Cipher.DECRYPT_MODE, keyPair.getSecretKey(), ivParameterSpec);

            byte[] decrypted = aesCipherDecrypt.doFinal(encryptedPayload.payload);

            Mac sha256MacDecrypt = Mac.getInstance(MAC_ALGORITHM);
            sha256MacDecrypt.init(keyPair.getAuthenticationKey());

            byte[] decryptedMac = sha256MacDecrypt.doFinal(decrypted);

            if (!Arrays.equals(decryptedMac, encryptedPayload.mac)) {
                throw new MACException();
            }

            decode(decrypted);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encodeByteArrays(List<byte[]> byteArrays) {

        int capacity = 1 + byteArrays.size() * 4;
        for (byte[] byteArray : byteArrays) {
            capacity += byteArray.length;
        }

        byte[] encoded = new byte[capacity];
        encoded[0] = (byte) byteArrays.size();

        int cur = 1;
        for (byte[] byteArray : byteArrays) {
            int length = byteArray.length;
            encoded[cur++] = (byte) (length >>> 24);
            encoded[cur++] = (byte) (length >>> 16);
            encoded[cur++] = (byte) (length >>> 8);
            encoded[cur++] = (byte) length;
        }

        for (byte[] byteArray : byteArrays) {
            System.arraycopy(byteArray, 0, encoded, cur, byteArray.length);
            cur += byteArray.length;
        }

        return encoded;
    }

    public static List<byte[]> decodeByteArrays(byte[] encoded) {

        int fieldCount = encoded[0];

        List<byte[]> byteArrays = new ArrayList<byte[]>(fieldCount);

        int current = fieldCount * 4 + 1;
        for (int i = 0; i < fieldCount; i++) {

            int length = encoded[4 * i + 1] << 24 | (encoded[4 * i + 2] & 0xFF) << 16
                    | (encoded[4 * i + 3] & 0xFF) << 8 | (encoded[4 * i + 4] & 0xFF);

            byte[] byteArray = new byte[length];

            System.arraycopy(encoded, current, byteArray, 0, length);
            byteArrays.add(byteArray);

            current += length;
        }

        return byteArrays;
    }
}

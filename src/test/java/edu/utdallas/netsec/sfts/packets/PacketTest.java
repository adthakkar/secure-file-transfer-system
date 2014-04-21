package edu.utdallas.netsec.sfts.packets;

import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public class PacketTest {

    @Test
    public void testEncodeDecodeSingleArray() throws Exception {

        List<byte[]> byteArrays = new ArrayList<byte[]>();
        byteArrays.add(RandomUtils.getSecureRandomBytes(11));

        byte[] encoded = Packet.encodeByteArrays(byteArrays);
        List<byte[]> byteArraysDecoded = Packet.decodeByteArrays(encoded);

        Assert.assertEquals(byteArraysDecoded.size(), byteArrays.size());
        for (int i = 0; i < byteArrays.size(); i++) {
            Assert.assertEquals(byteArraysDecoded.get(i), byteArrays.get(i));
        }
    }

    @Test
    public void testEncodeDecodeByteArrays() throws Exception {

        List<byte[]> byteArrays = new ArrayList<byte[]>();

        byteArrays.add(RandomUtils.getSecureRandomBytes(11));
        byteArrays.add(RandomUtils.getSecureRandomBytes(3));
        byteArrays.add(RandomUtils.getSecureRandomBytes(7));
        byteArrays.add(RandomUtils.getSecureRandomBytes(2));
        byteArrays.add(RandomUtils.getSecureRandomBytes(5));

        byte[] encoded = Packet.encodeByteArrays(byteArrays);
        List<byte[]> byteArraysDecoded = Packet.decodeByteArrays(encoded);

        Assert.assertEquals(byteArraysDecoded.size(), byteArrays.size());
        for (int i = 0; i < byteArrays.size(); i++) {
            Assert.assertEquals(byteArraysDecoded.get(i), byteArrays.get(i));
        }
    }

    @Test
    public void testEncodeDecodeByteArraysRandom() throws Exception {

        SecureRandom secureRandom = new SecureRandom();
        List<byte[]> byteArrays = new ArrayList<byte[]>();

        for (int i = 0; i < 100; i++) {
            byteArrays.add(RandomUtils.getSecureRandomBytes(secureRandom.nextInt(8) + 2));
        }

        byte[] encoded = Packet.encodeByteArrays(byteArrays);
        List<byte[]> byteArraysDecoded = Packet.decodeByteArrays(encoded);

        Assert.assertEquals(byteArraysDecoded.size(), byteArrays.size());
        for (int i = 0; i < byteArrays.size(); i++) {
            Assert.assertEquals(byteArraysDecoded.get(i), byteArrays.get(i));
        }
    }
}

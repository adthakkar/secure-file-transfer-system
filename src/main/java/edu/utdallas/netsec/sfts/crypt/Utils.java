package edu.utdallas.netsec.sfts.crypt;

import java.nio.ByteBuffer;

/**
 * @author Fahad Shaon
 */
public class Utils {

    public static byte[] toByteArrayByByteBuffer(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] toByteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value};
    }

    public static int fromByteArrayByByteBuffer(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

}

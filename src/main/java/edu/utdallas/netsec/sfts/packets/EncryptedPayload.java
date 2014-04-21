package edu.utdallas.netsec.sfts.packets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public class EncryptedPayload implements Serializable {

    public byte packetId;
    public byte[] iv;
    public byte[] payload;
    public byte[] mac;

    public byte[] encode() {


        List<byte[]> byteArrayList = new ArrayList<byte[]>();

        byteArrayList.add(new byte[]{packetId});
        byteArrayList.add(iv);
        byteArrayList.add(payload);
        byteArrayList.add(mac);

        return Packet.encodeByteArrays(byteArrayList);
    }

    public void decode(byte[] encoded) {

        byte fields = encoded[0];

        if (fields != 4) {
            throw new RuntimeException("Number of fields information is invalid.");
        }

        List<byte[]> byteArrayList = Packet.decodeByteArrays(encoded);

        this.packetId = byteArrayList.get(0)[0];
        this.iv = byteArrayList.get(1);
        this.payload = byteArrayList.get(2);
        this.mac = byteArrayList.get(3);
    }


}

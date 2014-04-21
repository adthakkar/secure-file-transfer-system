package edu.utdallas.netsec.sfts.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public class ClientRequest extends Packet {

    public String clientName;
    public String departServer;
    public byte[] nonce;

    public byte[] encode() {

        if (clientName == null || departServer == null || nonce == null
                || clientName.length() == 0 || departServer.length() == 0 || nonce.length == 0) {
            throw new RuntimeException("All the fields in a packet must exists");
        }

        List<byte[]> byteArrayList = new ArrayList<byte[]>();

        byteArrayList.add(clientName.getBytes());
        byteArrayList.add(departServer.getBytes());
        byteArrayList.add(nonce);

        return Packet.encodeByteArrays(byteArrayList);
    }

    public void decode(byte[] encoded) {

        checkHeader(encoded);

        List<byte[]> byteArrayList = Packet.decodeByteArrays(encoded);

        this.clientName = new String(byteArrayList.get(0));
        this.departServer = new String(byteArrayList.get(1));
        this.nonce = byteArrayList.get(2);
    }

    @Override
    public byte getPacketId() {
        return 1;
    }

    @Override
    public byte getFieldCount() {
        return 3;
    }
}

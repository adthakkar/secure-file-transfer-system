package edu.utdallas.netsec.sfts.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public class AuthServerResponse extends Packet {

    public String clientName;
    public byte[] sessionKeyClientMaster;
    public byte[] sessionKeyClientDepartment;
    public byte[] masterServerToken;
    public byte[] nonce;

    @Override
    public byte[] encode() {

        List<byte[]> byteArrays = new ArrayList<byte[]>();

        byteArrays.add(clientName.getBytes());
        byteArrays.add(sessionKeyClientMaster);
        byteArrays.add(sessionKeyClientDepartment);
        byteArrays.add(masterServerToken);
        byteArrays.add(nonce);

        return Packet.encodeByteArrays(byteArrays);
    }

    @Override
    public void decode(byte[] encoded) {

        checkHeader(encoded);

        List<byte[]> byteArrays = Packet.decodeByteArrays(encoded);

        this.clientName = new String(byteArrays.get(0));
        this.sessionKeyClientMaster = byteArrays.get(1);
        this.sessionKeyClientDepartment = byteArrays.get(2);
        this.masterServerToken = byteArrays.get(3);
        this.nonce = byteArrays.get(4);
    }

    @Override
    public byte getPacketId() {
        return 4;
    }

    @Override
    public byte getFieldCount() {
        return 5;
    }
}

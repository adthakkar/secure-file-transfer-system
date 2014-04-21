package edu.utdallas.netsec.sfts.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public class MasterServerTicket extends Packet {

    public String clientName;
    public String departServer;
    public byte[] sessionKeyClientMaster;
    public byte[] sessionKeyMasterDepartment;
    public byte[] departmentServerToken;
    public byte[] nonce;

    @Override
    public byte[] encode() {

        byte[] clientNameByte = clientName.getBytes();
        byte[] departServerByte = departServer.getBytes();

        List<byte[]> byteArrays = new ArrayList<byte[]>();

        byteArrays.add(clientNameByte);
        byteArrays.add(departServerByte);
        byteArrays.add(sessionKeyClientMaster);
        byteArrays.add(sessionKeyMasterDepartment);
        byteArrays.add(departmentServerToken);
        byteArrays.add(nonce);

        return Packet.encodeByteArrays(byteArrays);
    }

    @Override
    public void decode(byte[] encoded) {
        checkHeader(encoded);

        List<byte[]> byteArrays = Packet.decodeByteArrays(encoded);

        this.clientName = new String(byteArrays.get(0));
        this.departServer = new String(byteArrays.get(1));
        this.sessionKeyClientMaster = byteArrays.get(2);
        this.sessionKeyMasterDepartment = byteArrays.get(3);
        this.departmentServerToken = byteArrays.get(4);
        this.nonce = byteArrays.get(5);
    }

    @Override
    public byte getPacketId() {
        return 3;
    }

    @Override
    public byte getFieldCount() {
        return 6;
    }
}

package edu.utdallas.netsec.sfts.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Fahad Shaon
 */
public class DepartmentServerTicket extends Packet {

    public String clientName;
    public byte[] sessionKeyClientDepartment;
    public byte[] sessionKeyMasterDepartment;
    public byte[] nonce;


    @Override
    public byte[] encode() {

        byte[] clientNameByte = clientName.getBytes();

        List<byte[]> byteArrayList = new ArrayList<byte[]>();

        byteArrayList.add(clientNameByte);
        byteArrayList.add(sessionKeyClientDepartment);
        byteArrayList.add(sessionKeyMasterDepartment);
        byteArrayList.add(nonce);

        return Packet.encodeByteArrays(byteArrayList);
    }

    @Override
    public void decode(byte[] encoded) {

        checkHeader(encoded);

        List<byte[]> byteArrayList = Packet.decodeByteArrays(encoded);

        this.clientName = new String(byteArrayList.get(0));
        this.sessionKeyClientDepartment = byteArrayList.get(1);
        this.sessionKeyMasterDepartment = byteArrayList.get(2);
        this.nonce = byteArrayList.get(3);
    }

    @Override
    public byte getPacketId() {
        return 2;
    }

    @Override
    public byte getFieldCount() {
        return 4;
    }
}

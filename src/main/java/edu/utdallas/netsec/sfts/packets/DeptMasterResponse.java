/**
 * 
 */
package edu.utdallas.netsec.sfts.packets;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aditya
 *
 */
public class DeptMasterResponse extends Packet
{
  public String clientName;
  public byte[] deptClientToken;
  public byte[] nonce;

  @Override
  public byte[] encode()
  {
    byte[] clientNameByte = clientName.getBytes();

    List<byte[]> byteArrays = new ArrayList<byte[]>();

    byteArrays.add(clientNameByte);
    byteArrays.add(deptClientToken);
    byteArrays.add(nonce);

    return Packet.encodeByteArrays(byteArrays);
  }

  @Override
  public void decode(byte[] encoded)
  {
    checkHeader(encoded);

    List<byte[]> byteArrays = Packet.decodeByteArrays(encoded);

    this.clientName = new String(byteArrays.get(0));
    this.deptClientToken = byteArrays.get(1);
    this.nonce = byteArrays.get(2);
  }

  @Override
  public byte getPacketId()
  {
    return 9;
  }

  @Override
  public byte getFieldCount()
  {
    return 3;
  }

}

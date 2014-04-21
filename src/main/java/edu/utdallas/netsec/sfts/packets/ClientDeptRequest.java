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
public class ClientDeptRequest extends Packet
{
  public String clientName;
  public String reqFileName;
  public byte[] nonce;

  @Override
  public byte[] encode()
  {
    List<byte[]> byteArrays = new ArrayList<byte[]>();

    byteArrays.add(clientName.getBytes());
    byteArrays.add(reqFileName.getBytes());
    byteArrays.add(nonce);

    return Packet.encodeByteArrays(byteArrays);
  }

  @Override
  public void decode(byte[] encoded)
  {
    checkHeader(encoded);

    List<byte[]> byteArrays = Packet.decodeByteArrays(encoded);

    this.clientName = new String(byteArrays.get(0));
    this.reqFileName = new String(byteArrays.get(1));
    this.nonce = byteArrays.get(2);
  }

  @Override
  public byte getPacketId()
  {
    return 5;
  }

  @Override
  public byte getFieldCount()
  {
    return 3;
  }

}

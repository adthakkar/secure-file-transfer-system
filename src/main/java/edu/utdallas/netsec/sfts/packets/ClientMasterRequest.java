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
public class ClientMasterRequest extends Packet
{
  public String clientName;
  public byte[] clientDeptToken;
  public byte[] nonce;

  @Override
  public byte[] encode()
  {
    List<byte[]> byteArrays = new ArrayList<byte[]>();

    byteArrays.add(clientName.getBytes());
    byteArrays.add(clientDeptToken);
    byteArrays.add(nonce);

    return Packet.encodeByteArrays(byteArrays);
  }

  @Override
  public void decode(byte[] encoded)
  {
    checkHeader(encoded);

    List<byte[]> byteArrays = Packet.decodeByteArrays(encoded);

    this.clientName = new String(byteArrays.get(0));
    this.clientDeptToken = byteArrays.get(1);
    this.nonce = byteArrays.get(2);
  }

  @Override
  public byte getPacketId()
  {
    return 6;
  }

  @Override
  public byte getFieldCount()
  {
    return 3;
  }

}

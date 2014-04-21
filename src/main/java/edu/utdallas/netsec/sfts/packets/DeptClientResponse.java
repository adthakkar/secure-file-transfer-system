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
public class DeptClientResponse extends Packet
{
  public String clientName;
  public Integer respCode;
  public byte[] fileContent;
  public byte[] nonce;

  @Override
  public byte[] encode()
  {
    List<byte[]> byteArrays = new ArrayList<byte[]>();

    byteArrays.add(clientName.getBytes());
    byteArrays.add(respCode.toString().getBytes());
    byteArrays.add(fileContent);
    byteArrays.add(nonce);

    return Packet.encodeByteArrays(byteArrays);
  }

  @Override
  public void decode(byte[] encoded)
  {
    checkHeader(encoded);

    List<byte[]> byteArrays = Packet.decodeByteArrays(encoded);

    this.clientName = new String(byteArrays.get(0));
    this.respCode = Integer.parseInt(new String(byteArrays.get(1)));
    this.fileContent = byteArrays.get(2);
    this.nonce = byteArrays.get(3);
  }

  @Override
  public byte getPacketId()
  {
    return 8;
  }

  @Override
  public byte getFieldCount()
  {
    return 4;
  }

}

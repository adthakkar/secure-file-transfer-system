package edu.utdallas.netsec.sfts.packets;

import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fahad Shaon
 */
public class DepartmentServerTicketTest {

    @Test
    public void testEncodeDecode() throws Exception {

        DepartmentServerTicket dst = new DepartmentServerTicket();

        dst.clientName = "First Client";
        dst.sessionKeyClientDepartment = RandomUtils.getSecureRandomBytes(64);
        dst.sessionKeyMasterDepartment = RandomUtils.getSecureRandomBytes(64);
        dst.nonce = RandomUtils.getSecureRandomBytes(8);

        byte[] encoded = dst.encode();

        DepartmentServerTicket dstDecoded = new DepartmentServerTicket();

        Assert.assertNull(dstDecoded.clientName);
        Assert.assertNull(dstDecoded.sessionKeyClientDepartment);
        Assert.assertNull(dstDecoded.sessionKeyMasterDepartment);
        Assert.assertNull(dstDecoded.nonce);

        dstDecoded.decode(encoded);

        Assert.assertEquals(dstDecoded.clientName, dst.clientName);
        Assert.assertEquals(dstDecoded.sessionKeyClientDepartment, dst.sessionKeyClientDepartment);
        Assert.assertEquals(dstDecoded.sessionKeyMasterDepartment, dst.sessionKeyMasterDepartment);
        Assert.assertEquals(dstDecoded.nonce, dst.nonce);
    }
}

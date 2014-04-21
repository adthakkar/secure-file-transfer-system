package edu.utdallas.netsec.sfts.packets;

import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fahad Shaon
 */
public class MasterServerTicketTest {

    @Test
    public void testEncodeDecode() throws Exception {

        MasterServerTicket masterServerTicket = new MasterServerTicket();
        masterServerTicket.clientName = "First client";
        masterServerTicket.departServer = "finance";
        masterServerTicket.sessionKeyClientMaster = RandomUtils.getSecureRandomBytes(64);
        masterServerTicket.sessionKeyMasterDepartment = RandomUtils.getSecureRandomBytes(64);

        DepartmentServerTicket dst = new DepartmentServerTicket();

        dst.clientName = "First Client";
        dst.sessionKeyClientDepartment = RandomUtils.getSecureRandomBytes(64);
        dst.sessionKeyMasterDepartment = RandomUtils.getSecureRandomBytes(64);
        dst.nonce = RandomUtils.getSecureRandomBytes(8);

        byte[] encodedDST = dst.encode();

        masterServerTicket.departmentServerToken = encodedDST;
        masterServerTicket.nonce = RandomUtils.getSecureRandomBytes(8);

        byte[] encoded = masterServerTicket.encode();

        MasterServerTicket masterServerTicketDecoded = new MasterServerTicket();
        masterServerTicketDecoded.decode(encoded);

        Assert.assertEquals(masterServerTicketDecoded.clientName, masterServerTicket.clientName);
        Assert.assertEquals(masterServerTicketDecoded.departServer, masterServerTicket.departServer);
        Assert.assertEquals(masterServerTicketDecoded.sessionKeyClientMaster, masterServerTicket.sessionKeyClientMaster);
        Assert.assertEquals(masterServerTicketDecoded.sessionKeyMasterDepartment, masterServerTicket.sessionKeyMasterDepartment);
        Assert.assertEquals(masterServerTicketDecoded.departmentServerToken, masterServerTicket.departmentServerToken);
        Assert.assertEquals(masterServerTicketDecoded.departmentServerToken, encodedDST);
        Assert.assertEquals(masterServerTicketDecoded.nonce, masterServerTicket.nonce);

        System.out.println("Master Server Ticket Size: " + encoded.length);
    }
}

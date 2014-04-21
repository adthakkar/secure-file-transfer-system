package edu.utdallas.netsec.sfts.packets;

import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Fahad Shaon
 */
public class AuthServerResponseTest {

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

        byte[] encodedMST = masterServerTicket.encode();

        AuthServerResponse authServerResponse = new AuthServerResponse();

        authServerResponse.clientName = "First Client";
        authServerResponse.sessionKeyClientMaster = RandomUtils.getSecureRandomBytes(64);
        authServerResponse.sessionKeyClientDepartment = RandomUtils.getSecureRandomBytes(64);
        authServerResponse.masterServerToken = encodedMST;
        authServerResponse.nonce = RandomUtils.getSecureRandomBytes(8);

        byte[] encoded = authServerResponse.encode();

        AuthServerResponse authServerResponseDecoded = new AuthServerResponse();
        authServerResponseDecoded.decode(encoded);

        Assert.assertEquals(authServerResponseDecoded.clientName, authServerResponse.clientName);
        Assert.assertEquals(authServerResponseDecoded.sessionKeyClientMaster, authServerResponse.sessionKeyClientMaster);
        Assert.assertEquals(authServerResponseDecoded.sessionKeyClientDepartment, authServerResponse.sessionKeyClientDepartment);
        Assert.assertEquals(authServerResponseDecoded.masterServerToken, authServerResponse.masterServerToken);
        Assert.assertEquals(authServerResponseDecoded.nonce, authServerResponse.nonce);
    }
}

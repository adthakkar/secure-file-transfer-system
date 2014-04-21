package edu.utdallas.netsec.sfts.packets;

import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.SecureRandom;

/**
 * @author Fahad Shaon
 */
public class ClientRequestTest {

    @Test
    public void testEncode() throws Exception {

        ClientRequest clientRequest = new ClientRequest();

        clientRequest.clientName = "First client";
        clientRequest.departServer = "finance";
        clientRequest.nonce = RandomUtils.getSecureRandomBytes(8);

        byte[] encoded = clientRequest.encode();

        ClientRequest clientRequestDecoded = new ClientRequest();
        clientRequestDecoded.decode(encoded);

        Assert.assertEquals(clientRequest.clientName, clientRequestDecoded.clientName);
        Assert.assertEquals(clientRequest.departServer, clientRequestDecoded.departServer);
        Assert.assertEquals(clientRequest.nonce, clientRequestDecoded.nonce);
    }
}

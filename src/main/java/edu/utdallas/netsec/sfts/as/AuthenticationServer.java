package edu.utdallas.netsec.sfts.as;

import edu.utdallas.netsec.sfts.crypt.ConfigReader;
import edu.utdallas.netsec.sfts.crypt.KeyManager;
import edu.utdallas.netsec.sfts.crypt.KeyPair;
import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import edu.utdallas.netsec.sfts.packets.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Fahad Shaon
 */
public class AuthenticationServer {

    private Properties asProperties;
    private String configDirectory;
    private List<String> clientList;
    private List<String> departmentServerList;

    private void setConfigDirectory(String configDirectory) {

        String absPath = new File(configDirectory).getAbsolutePath();
        String configFile = absPath + "/as.properties";

        if (!new File(configFile).exists()) {
            throw new RuntimeException("Config file 'as.properties' not found in: " + configDirectory);
        }

        this.configDirectory = configDirectory;
    }

    private void readConfig() throws IOException {
        String configFile = new File(configDirectory).getAbsolutePath() + "/as.properties";
        System.out.println("Reading config file: " + configFile);
        asProperties = ConfigReader.readConfig(configFile);
    }

    private boolean isValidDepartment(String department) {

        if (departmentServerList == null) {
            departmentServerList = new ArrayList<String>();
            for (String d : asProperties.getProperty("department.list").split(",")) {
                String trimmed = d.trim();
                if (!trimmed.isEmpty()) {
                    departmentServerList.add(trimmed);
                }
            }
        }

        return departmentServerList.contains(department);
    }

    private boolean isValidClientName(String clientName) {

        if (clientList == null) {

            clientList = new ArrayList<String>();
            for (String c : asProperties.getProperty("client.list").split(",")) {
                String trimmed = c.trim();
                if (!trimmed.isEmpty()) {
                    clientList.add(trimmed);
                }
            }
        }

        return clientList.contains(clientName);
    }

    private KeyPair getMasterServerKey() throws IOException {
        return KeyManager.readKeyPairFromFile(asProperties.getProperty("master.key"));
    }

    private KeyPair getClientKey(String clientName) throws IOException {

        String clientKey = String.format("client.%s.key", clientName);
        return KeyManager.readKeyPairFromFile(asProperties.getProperty(clientKey));
    }

    private KeyPair getDepartmentServerKey(String departmentName) throws IOException {

        String departmentKey = String.format("department.%s.key", departmentName);
        return KeyManager.readKeyPairFromFile(asProperties.getProperty(departmentKey));
    }

    public void run() throws IOException, ClassNotFoundException {

        readConfig();
        int portNumber = Integer.parseInt(asProperties.getProperty("as.port"));

        ServerSocket serverSocket = new ServerSocket(portNumber);
        System.out.println("Opening a socket at " + portNumber);

        Socket clientSocket = serverSocket.accept();
        System.out.println("Accepted a connection from LocalAddress: " + clientSocket.getLocalAddress()
                + ", InetAddress:" + clientSocket.getInetAddress());

        ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
        String clientName = (String) objectInputStream.readObject();

        if (!isValidClientName(clientName)) {
            System.out.println("Invalid client.");
            return;
        }

        KeyPair clientKeyPair = getClientKey(clientName);
        EncryptedPayload encryptedPayload = (EncryptedPayload) objectInputStream.readObject();

        ClientRequest clientRequest = new ClientRequest();
        clientRequest.decrypt(clientKeyPair, encryptedPayload);

        KeyPair masterServerKeyPair = getMasterServerKey();
        if (!isValidDepartment(clientRequest.departServer)) {
            System.out.println("Invalid department.");
            return;
        }

        KeyPair departmentServerKeyPair = getDepartmentServerKey(clientRequest.departServer);

        //generating keys
        byte[] sessionKeyClientDepartment = RandomUtils.getSecureRandomBytes(64);
        byte[] sessionKeyMasterDepartment = RandomUtils.getSecureRandomBytes(64);
        byte[] sessionKeyClientMaster = RandomUtils.getSecureRandomBytes(64);

        //generating the response packet
        DepartmentServerTicket departmentServerTicket = new DepartmentServerTicket();

        departmentServerTicket.clientName = clientRequest.clientName;
        departmentServerTicket.sessionKeyClientDepartment = sessionKeyClientDepartment;
        departmentServerTicket.sessionKeyMasterDepartment = sessionKeyMasterDepartment;
        departmentServerTicket.nonce = RandomUtils.getNonce();

        EncryptedPayload dsEncryptedTicket = departmentServerTicket.encrypt(departmentServerKeyPair);

        MasterServerTicket masterServerTicket = new MasterServerTicket();
        masterServerTicket.clientName = clientRequest.clientName;
        masterServerTicket.departServer = clientRequest.departServer;
        masterServerTicket.sessionKeyClientMaster = sessionKeyClientMaster;
        masterServerTicket.sessionKeyMasterDepartment = sessionKeyMasterDepartment;
        masterServerTicket.departmentServerToken = dsEncryptedTicket.encode();
        masterServerTicket.nonce = RandomUtils.getNonce();

        EncryptedPayload msEncryptedTicket = masterServerTicket.encrypt(masterServerKeyPair);

        AuthServerResponse authServerResponse = new AuthServerResponse();
        authServerResponse.clientName = clientRequest.clientName;
        authServerResponse.sessionKeyClientMaster = sessionKeyClientMaster;
        authServerResponse.sessionKeyClientDepartment = sessionKeyClientDepartment;
        authServerResponse.masterServerToken = msEncryptedTicket.encode();
        authServerResponse.nonce = clientRequest.nonce;

        EncryptedPayload encryptedPayloadClientResponse = authServerResponse.encrypt(clientKeyPair);

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        objectOutputStream.writeObject(encryptedPayloadClientResponse);
        objectOutputStream.flush();

        System.out.println("Server Exiting");
    }

    public static void printUsages() {
        System.out.println("Invalid Arguments");
        System.out.println("Usages: ./run.sh as /path/to/auth_server_config_dir/");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        if (args.length != 1) {
            printUsages();
            return;
        }

        AuthenticationServer authenticationServer = new AuthenticationServer();
        authenticationServer.setConfigDirectory(args[0]);
        authenticationServer.run();
    }
}

package edu.utdallas.netsec.sfts.client;

import edu.utdallas.netsec.sfts.crypt.ConfigReader;
import edu.utdallas.netsec.sfts.crypt.FileUtils;
import edu.utdallas.netsec.sfts.crypt.KeyManager;
import edu.utdallas.netsec.sfts.crypt.KeyPair;
import edu.utdallas.netsec.sfts.crypt.RandomUtils;
import edu.utdallas.netsec.sfts.packets.AuthServerResponse;
import edu.utdallas.netsec.sfts.packets.ClientRequest;
import edu.utdallas.netsec.sfts.packets.EncryptedPayload;
import edu.utdallas.netsec.sfts.packets.ClientDeptRequest;
import edu.utdallas.netsec.sfts.packets.ClientMasterRequest;
import edu.utdallas.netsec.sfts.packets.DeptClientResponse;
import edu.utdallas.netsec.sfts.packets.MasterClientResponse;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Fahad Shaon
 */
public class Client
{

  private Properties clientProperties;
  private String configDirectory, deptName, fileName;
  private List<String> departmentServerList;
  private KeyPair sessionKeyClientMaster;
  private KeyPair sessionKeyClientDepartment;

  private void setConfigDirectory(String configDirectory)
  {

    String absPath = new File(configDirectory).getAbsolutePath();
    String configFile = absPath + "/client.properties";
    String keyFile = absPath + "/as_client.key";

    if (!new File(configFile).exists())
    {
      throw new RuntimeException(
          "Config file 'client.properties' not found in - " + configDirectory);
    }

    if (!new File(keyFile).exists())
    {
      throw new RuntimeException("Key file 'as_client.key' not found in - "
          + configDirectory);
    }

    this.configDirectory = configDirectory;
  }

  private void readConfig() throws IOException
  {
    String configFile = new File(configDirectory).getAbsolutePath()
        + "/client.properties";
    System.out.println("Reading config file: " + configFile);
    clientProperties = ConfigReader.readConfig(configFile);
  }

  private boolean readDepartmentList(String deptName)
  {

    departmentServerList = new ArrayList<String>();
    for (String d : clientProperties.getProperty("department.list").split(","))
    {
      String trimmed = d.trim();
      if (!trimmed.isEmpty())
      {
        departmentServerList.add(trimmed);
      }
    }

    return departmentServerList.contains(deptName);
  }

  private KeyPair getAuthClientKey() throws IOException
  {
    return KeyManager.readKeyPairFromFile(new File(configDirectory)
        .getAbsolutePath() + "/as_client.key");
  }

  public void run() throws IOException, ClassNotFoundException
  {
    readConfig();

    String asHostName = clientProperties.getProperty("as.hostname");
    int asPortNumber = Integer.parseInt(clientProperties.getProperty("as.port"));
    String clientName = clientProperties.getProperty("client.name");

    System.out.println("Connecting to AS with HostName: " + asHostName 
                        + ", PortNumber: " + asPortNumber);
    Socket asSocket = new Socket(asHostName, asPortNumber);

    KeyPair authClientKeyPair = getAuthClientKey();

    ClientRequest clientRequest = new ClientRequest();
    clientRequest.clientName = clientName;
    if (readDepartmentList(this.deptName))
    {
      clientRequest.departServer = this.deptName;
    } 
    else
    {
      asSocket.close();
      System.out.println("Department name - "+deptName+" is invalid.");
      return;
    }

    byte[] nonce = RandomUtils.getNonce();
    clientRequest.nonce = nonce;

    EncryptedPayload encryptedPayload = clientRequest
        .encrypt(authClientKeyPair);

    ObjectOutputStream objectOutputStream = new ObjectOutputStream(asSocket.getOutputStream());
    objectOutputStream.writeObject(clientName);
    objectOutputStream.writeObject(encryptedPayload);
    objectOutputStream.flush();

    ObjectInputStream objectInputStream = new ObjectInputStream(asSocket.getInputStream());
    EncryptedPayload encryptedPayloadReceived = (EncryptedPayload) objectInputStream.readObject();

    AuthServerResponse authServerResponse = new AuthServerResponse();
    authServerResponse.decrypt(authClientKeyPair, encryptedPayloadReceived);

    System.out.println("Auth Server response decrypted");

    if (!Arrays.equals(authServerResponse.nonce, nonce) || !clientName.equals(authServerResponse.clientName))
    {
      asSocket.close();
      throw new RuntimeException("Client name or nonce mismatch. Expected client name-"+clientName);
    }
    
    asSocket.close();
    sessionKeyClientMaster = KeyManager.generateKeyPair(authServerResponse.sessionKeyClientMaster);
    sessionKeyClientDepartment = KeyManager.generateKeyPair(authServerResponse.sessionKeyClientDepartment);

    EncryptedPayload masterTicket = new EncryptedPayload();
    masterTicket.decode(authServerResponse.masterServerToken);

    String masterHostName = clientProperties.getProperty("master.hostname");
    int masterPortNumber = Integer.parseInt(clientProperties.getProperty("master.port"));

    System.out.println("Connecting to Master File Server with HostName: "
                        + masterHostName + ", PortNumber: " + masterPortNumber);
    Socket masterSocket = new Socket(masterHostName, masterPortNumber);

    ObjectOutputStream outputStream = new ObjectOutputStream(masterSocket.getOutputStream());
    outputStream.writeObject(masterTicket);

    System.out.println("Ticket sent to Master File Server...");
    System.out.println("Generating the File transfer Request...");

    ClientDeptRequest deptRequest = new ClientDeptRequest();
    deptRequest.clientName = clientName;
    deptRequest.nonce = RandomUtils.getNonce();
    deptRequest.reqFileName = this.fileName;

    EncryptedPayload encryptedDeptRequest = deptRequest.encrypt(sessionKeyClientDepartment);

    ClientMasterRequest masterRequest = new ClientMasterRequest();
    masterRequest.clientName = clientName;
    masterRequest.nonce = RandomUtils.getNonce();
    masterRequest.clientDeptToken = encryptedDeptRequest.encode();

    EncryptedPayload encryptedMasterRequest = masterRequest.encrypt(sessionKeyClientMaster);

    outputStream.writeObject(encryptedMasterRequest);

    objectInputStream = new ObjectInputStream(masterSocket.getInputStream());
    System.out.println("File Transfer Request sent... ");
    
    encryptedPayloadReceived = (EncryptedPayload) objectInputStream.readObject();

    System.out.println("Recived response for the File Transfer Request");
    MasterClientResponse mClientResponse = new MasterClientResponse();
    mClientResponse.decrypt(sessionKeyClientMaster, encryptedPayloadReceived);

    if (!Arrays.equals(mClientResponse.nonce, masterRequest.nonce) || 
        !mClientResponse.clientName.equals(clientName))
    {
      masterSocket.close();
      throw new RuntimeException("Client name or nonce mismatch in response. " +
                                 "Expected client name-"+clientName + ". Received client name -" +
                                  mClientResponse.clientName);
    }

    EncryptedPayload departmentResponse = new EncryptedPayload();
    departmentResponse.decode(mClientResponse.deptClientToken);

    DeptClientResponse dClientResponse = new DeptClientResponse();
    dClientResponse.decrypt(sessionKeyClientDepartment, departmentResponse);

    if(dClientResponse.respCode == 0 && Arrays.equals(dClientResponse.nonce, deptRequest.nonce))
    {
      String fileName = configDirectory + "/" + this.fileName;
      File newFile = new File(fileName);
      if (newFile.createNewFile())
      {
        FileUtils.writeFileBinary(fileName, dClientResponse.fileContent);
      }
      System.out.println("File Transfer Complete... Check in the config directory for the file... ");
    }
    System.out.println("Client Exiting");
    masterSocket.close();
  }

  public static void printUsages()
  {
    System.out.println("Invalid Arguments");
    System.out.println("Usages: ./run.sh client /path/to/client_config_dir/ departmentname filename");
  }

  public static void main(String[] args) throws IOException,
      ClassNotFoundException
  {

    if (args.length != 3)
    {
      printUsages();
      return;
    }

    Client client = new Client();
    client.setConfigDirectory(args[0]);
    client.deptName = args[1];
    client.fileName = args[2];
    client.run();
  }
}

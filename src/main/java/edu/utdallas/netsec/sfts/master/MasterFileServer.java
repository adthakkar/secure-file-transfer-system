package edu.utdallas.netsec.sfts.master;

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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MasterFileServer
{

  private Properties mfsProperties;
  private String configDirectory;
  private List<String> clientList;
  private List<String> departmentServerList;
  KeyPair sessionKeyClientMaster, sessionKeyMasterDepartment, masterKeyPair;

  private boolean isValidDepartment(String department)
  {
    if (departmentServerList == null)
    {
      departmentServerList = new ArrayList<String>();
      for (String d : mfsProperties.getProperty("department.list").split(","))
      {
        String trimmed = d.trim();
        if (!trimmed.isEmpty())
        {
          departmentServerList.add(trimmed);
        }
      }
    }

    return departmentServerList.contains(department);
  }

  private boolean isValidClientName(String clientName)
  {
    if (clientList == null)
    {
      clientList = new ArrayList<String>();
      for (String c : mfsProperties.getProperty("client.list").split(","))
      {
        String trimmed = c.trim();
        if (!trimmed.isEmpty())
        {
          clientList.add(trimmed);
        }
      }
    }

    return clientList.contains(clientName);
  }

  private void setConfigDirectory(String configDirectory)
  {

    String absPath = new File(configDirectory).getAbsolutePath();
    String configFile = absPath + "/master.properties";

    if (!new File(configFile).exists())
    {
      throw new RuntimeException("Config file 'master.properties' not found in: " + configDirectory);
    }

    this.configDirectory = configDirectory;
  }

  private void readConfig() throws IOException
  {
    String configFile = new File(configDirectory).getAbsolutePath() + "/master.properties";
    System.out.println("Reading config file: " + configFile);
    mfsProperties = ConfigReader.readConfig(configFile);
  }

  private KeyPair getMasterKey() throws IOException
  {
    return KeyManager.readKeyPairFromFile(new File(configDirectory).getAbsolutePath() + "/as_master.key");
  }

  public static void printUsages()
  {
    System.out.println("Invalid Arguments");
    System.out.println("Usages: ./run.sh as /path/to/master_file_server_config_dir/");
  }

  public void run() throws IOException, ClassNotFoundException
  {
    readConfig();
    int portNumber = Integer.parseInt(mfsProperties.getProperty("master.port"));
    ServerSocket serverSocket = new ServerSocket(portNumber);
    System.out.println("Opening a socket at " + portNumber);

    Socket clientSocket = serverSocket.accept();
    System.out.println("Accepted a connection from LocalAddress: "
                      + clientSocket.getLocalAddress() + ", InetAddress:"
                      + clientSocket.getInetAddress());

    ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    EncryptedPayload encryptedPayload = (EncryptedPayload) objectInputStream.readObject();
    masterKeyPair = getMasterKey();
    MasterServerTicket masterTicket = new MasterServerTicket();
    masterTicket.decrypt(masterKeyPair, encryptedPayload);

    if (!isValidClientName(masterTicket.clientName))
    {
      System.out.println("Invalid client-"+ masterTicket.clientName);
      serverSocket.close();
      return;
    }

    if (!isValidDepartment(masterTicket.departServer))
    {
      System.out.println("Invalid department-"+ masterTicket.departServer);
      serverSocket.close();
      return;
    }

    sessionKeyClientMaster = KeyManager.generateKeyPair(masterTicket.sessionKeyClientMaster);
    sessionKeyMasterDepartment = KeyManager.generateKeyPair(masterTicket.sessionKeyMasterDepartment);

    EncryptedPayload departmentServerTicket = new EncryptedPayload();
    departmentServerTicket.decode(masterTicket.departmentServerToken);

    String deptName = String.format("department.%s", masterTicket.departServer);
    portNumber = Integer.parseInt(mfsProperties.getProperty(deptName + ".port"));
    deptName = mfsProperties.getProperty(deptName + ".hostname");

    System.out.println("Connecting to Department "
                      + masterTicket.departServer + " with HostName: " + deptName
                      + " at PortNumber: " + portNumber);

    Socket dsocket = new Socket(deptName, portNumber);
    ObjectOutputStream outputStream = new ObjectOutputStream(dsocket.getOutputStream());
    outputStream.writeObject(departmentServerTicket);

    System.out.println("Ticket sent to Department" + masterTicket.departServer);

    encryptedPayload = (EncryptedPayload) objectInputStream.readObject();

    ClientMasterRequest cMasterRequest = new ClientMasterRequest();
    cMasterRequest.decrypt(sessionKeyClientMaster, encryptedPayload);

    System.out.println("File Transfer Request received from client");

    MasterDeptRequest mDeptRequest = new MasterDeptRequest();
    mDeptRequest.clientName = cMasterRequest.clientName;
    mDeptRequest.nonce = RandomUtils.getNonce();
    mDeptRequest.clientDeptToken = cMasterRequest.clientDeptToken;

    EncryptedPayload mDeptPayload = mDeptRequest.encrypt(sessionKeyMasterDepartment);
    outputStream.writeObject(mDeptPayload);

    ObjectInputStream inputStream = new ObjectInputStream(dsocket.getInputStream());
    encryptedPayload = (EncryptedPayload) inputStream.readObject();

    DeptMasterResponse dMasterResponse = new DeptMasterResponse();
    dMasterResponse.decrypt(sessionKeyMasterDepartment, encryptedPayload);

    if (!Arrays.equals(dMasterResponse.nonce, mDeptRequest.nonce) && 
        !dMasterResponse.clientName.equals(mDeptRequest.clientName))
    {
      dsocket.close();
      serverSocket.close();
      throw new RuntimeException("Mismatch in nonce or client name in DeptMasterResponse. "
                               + "Expected client name-"+ mDeptRequest.clientName);
    }

    MasterClientResponse mClientResponse = new MasterClientResponse();
    mClientResponse.nonce = cMasterRequest.nonce;
    mClientResponse.deptClientToken = dMasterResponse.deptClientToken;
    mClientResponse.clientName = cMasterRequest.clientName;

    EncryptedPayload encryptedResponse = mClientResponse.encrypt(sessionKeyClientMaster);
    outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    outputStream.writeObject(encryptedResponse);

    System.out.println("Forwarded "+masterTicket.departServer +
                       " department's response to client. Master File Server is exiting");
    dsocket.close();
    serverSocket.close();
  }

  public static void main(String[] args) throws IOException,
      ClassNotFoundException
  {

    if (args.length != 1)
    {
      printUsages();
      return;
    }

    MasterFileServer mfs = new MasterFileServer();
    mfs.setConfigDirectory(args[0]);
    mfs.run();
  }

}

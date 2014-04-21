package edu.utdallas.netsec.sfts.ds;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import edu.utdallas.netsec.sfts.crypt.ConfigReader;
import edu.utdallas.netsec.sfts.crypt.FileUtils;
import edu.utdallas.netsec.sfts.crypt.KeyManager;
import edu.utdallas.netsec.sfts.crypt.KeyPair;
import edu.utdallas.netsec.sfts.packets.ClientDeptRequest;
import edu.utdallas.netsec.sfts.packets.DepartmentServerTicket;
import edu.utdallas.netsec.sfts.packets.DeptClientResponse;
import edu.utdallas.netsec.sfts.packets.DeptMasterResponse;
import edu.utdallas.netsec.sfts.packets.EncryptedPayload;
import edu.utdallas.netsec.sfts.packets.MasterDeptRequest;

public class DepartmentServer
{
  private static Properties dsProperties;
  private String configDirectory;
  
  private void setConfigDirectory(String configDirectory) {

    String absPath = new File(configDirectory).getAbsolutePath();
    String configFile = absPath + "/department.properties";
    
    if (!new File(configFile).exists()) {
        throw new RuntimeException("Config file 'as.properties' not found in: " + configDirectory);
    }
    
    this.configDirectory = configDirectory;
  }
  
  private void readConfig() throws IOException {
    String configFile = new File(configDirectory).getAbsolutePath() + "/department.properties";
    System.out.println("Reading config file: " + configFile);
    dsProperties = ConfigReader.readConfig(configFile);
  }
  
  private KeyPair getAuthDeptKeyPair(String deptKeyFile) throws IOException
  {
    return KeyManager.readKeyPairFromFile(deptKeyFile);
  }
  
  public void run() throws IOException, ClassNotFoundException 
  {
    readConfig();
    
    String deptName = dsProperties.getProperty("department.name");
    String keyFile = new File(configDirectory).getAbsolutePath() + "/as_"+deptName+".key";
    
    if (!new File(keyFile).exists()) {
      throw new RuntimeException("Key file - "+keyFile+" not found");
    }
    
    //Listen for client connection on designated department server socket
    int portNumber = Integer.parseInt(dsProperties.getProperty("department.port"));

    ServerSocket dsSocket = new ServerSocket(portNumber);
    System.out.println("Opening a socket at " + portNumber);
    
    Socket clientSocket = dsSocket.accept();
    System.out.println("Accepted a connection from LocalAddress: " + clientSocket.getLocalAddress()
            + ", InetAddress:" + clientSocket.getInetAddress());
    
    ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
    
    //Gets the encrypted ticket for the session and decrypts it
    KeyPair asDeptKeyPair = getAuthDeptKeyPair(keyFile);
    EncryptedPayload encryptedPayload = (EncryptedPayload) objectInputStream.readObject();

    DepartmentServerTicket dsTicket = new DepartmentServerTicket();
    dsTicket.decrypt(asDeptKeyPair, encryptedPayload);
    System.out.println("Received Ticket for Client-"+dsTicket.clientName);
    
    //generate KeyPair for the session keys
    KeyPair clientDeptSessionKey = new KeyPair(dsTicket.sessionKeyClientDepartment);
    KeyPair deptMasterSessionKey = new KeyPair(dsTicket.sessionKeyMasterDepartment);
    
    //Get the request from the Master Server
    EncryptedPayload enMasterDeptReq = (EncryptedPayload) objectInputStream.readObject();
    MasterDeptRequest mdRequest = new MasterDeptRequest();
    mdRequest.decrypt(deptMasterSessionKey, enMasterDeptReq);
    
    //Get the encrypted client request embedded in request from master
    EncryptedPayload enClientDeptReq = new EncryptedPayload();
    enClientDeptReq.decode(mdRequest.clientDeptToken);
    ClientDeptRequest clientDeptReq = new ClientDeptRequest();
    clientDeptReq.decrypt(clientDeptSessionKey, enClientDeptReq);
    
    if(!mdRequest.clientName.equals(dsTicket.clientName) || 
       !clientDeptReq.clientName.equals(dsTicket.clientName))
    {
      dsSocket.close();
      throw new RuntimeException("Client Mismatch: expecting client-"+dsTicket.clientName);
    }
    
    String filePath = new File(configDirectory).getAbsolutePath() + "/"+clientDeptReq.reqFileName;
    
    //Form the response packet to the client and encrypt it
    DeptClientResponse deptClientResp = new DeptClientResponse();
    deptClientResp.clientName = clientDeptReq.clientName;
    deptClientResp.nonce = clientDeptReq.nonce;
    deptClientResp.respCode = -1;
    deptClientResp.fileContent = new byte[1];
    
    if (new File(filePath).exists()) {
      System.out.println("File '"+clientDeptReq.clientName +"' found");
      byte[] fileContent = FileUtils.readFileBinary(filePath);
      deptClientResp.fileContent = fileContent;
      deptClientResp.respCode = 0;
    }
    else
    {
      System.out.println("File '"+clientDeptReq.clientName+"' not found in "+configDirectory);
    }
    EncryptedPayload enDeptClientResp = deptClientResp.encrypt(clientDeptSessionKey);
    
    //form the response packet to the master and encrypt it
    DeptMasterResponse depMasterResp = new DeptMasterResponse();
    depMasterResp.clientName = mdRequest.clientName;
    depMasterResp.nonce = mdRequest.nonce;
    depMasterResp.deptClientToken = enDeptClientResp.encode();
    
    EncryptedPayload enDeptMasterResp = depMasterResp.encrypt(deptMasterSessionKey);
    
    //send the response to the master
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
    objectOutputStream.writeObject(enDeptMasterResp);
    objectOutputStream.flush();
    
    System.out.println("File Sent to Client. Exiting.");
    dsSocket.close();
  }
  
  public static void printUsages() {
    System.out.println("Invalid Arguments");
    System.out.println("Usages: ./run.sh client /path/to/department_config_dir/");
  }
  
  public static void main(String[] args) throws IOException, ClassNotFoundException {

    if (args.length != 1) {
        printUsages();
        return;
    }

    DepartmentServer deptServer = new DepartmentServer();
    deptServer.setConfigDirectory(args[0]);
    deptServer.run();
  }
}

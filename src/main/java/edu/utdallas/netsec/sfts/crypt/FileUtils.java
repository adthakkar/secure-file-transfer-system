package edu.utdallas.netsec.sfts.crypt;

import java.io.*;

/**
 * @author Fahad Shaon
 */
public class FileUtils {

    public static byte[] readFileBinary(String filename) throws IOException {

        File file = new File(filename);
        byte[] data = new byte[(int) file.length()];
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        dis.readFully(data);
        dis.close();

        return data;
    }

    public static void writeFileBinary(String filename, byte[]... dataArrays) throws IOException {

        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));

        for (byte[] data : dataArrays) {
            dos.write(data);
        }

        dos.flush();
        dos.close();
    }
}

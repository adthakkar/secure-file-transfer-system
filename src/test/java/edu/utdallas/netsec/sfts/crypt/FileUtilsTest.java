package edu.utdallas.netsec.sfts.crypt;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.security.SecureRandom;

/**
 * @author Fahad Shaon
 */
public class FileUtilsTest {


    @Test
    public void testReadWriteFileBinarySingleArray() throws Exception {

        String filename = "data/test/files/test-binary-rw.data";

        int size = 1024;
        byte[] randomBytes = new byte[size];
        new SecureRandom().nextBytes(randomBytes);

        FileUtils.writeFileBinary(filename, randomBytes);

        File file = new File(filename);
        Assert.assertEquals(file.length(), size);

        byte[] readRandomBytes = FileUtils.readFileBinary(filename);
        Assert.assertEquals(readRandomBytes, randomBytes);

        if (!file.delete()) {
            System.err.println("Temporary file (" + filename + ") created for testing wasn't deleted");
        }
    }


    @Test
    public void testReadWriteFileBinaryMultiArray() throws Exception {
        String filename = "data/test/files/test-binary-rw-multi.data";

        int size1 = 16;
        int size2 = 1024;
        byte[] randomBytes1 = new byte[size1];
        byte[] randomBytes2 = new byte[size2];

        new SecureRandom().nextBytes(randomBytes1);
        new SecureRandom().nextBytes(randomBytes2);

        FileUtils.writeFileBinary(filename, randomBytes1, randomBytes2);

        File file = new File(filename);
        Assert.assertEquals(file.length(), size1 + size2);

        byte[] readRandomBytes = FileUtils.readFileBinary(filename);


        byte[] readRandomBytes1 = new byte[size1];
        byte[] readRandomBytes2 = new byte[size2];

        System.arraycopy(readRandomBytes, 0, readRandomBytes1, 0, size1);
        System.arraycopy(readRandomBytes, size1, readRandomBytes2, 0, size2);

        Assert.assertEquals(readRandomBytes1, randomBytes1);
        Assert.assertEquals(readRandomBytes2, randomBytes2);

        if (!file.delete()) {
            System.err.println("Temporary file (" + filename + ") created for testing wasn't deleted");
        }
    }

}

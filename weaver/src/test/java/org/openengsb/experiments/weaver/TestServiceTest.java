package org.openengsb.experiments.weaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.experiments.weaver.internal.Activator;
import org.openengsb.experiments.weaver.internal.TestService;

public class TestServiceTest {

    private File testfile;
    private TestService service;

    @Before
    public void initiate() {
        testfile = new File("target/classes/org/openengsb/experiments/weaver/internal/Activator.class");
        service = new TestService();
    }

    private byte[] getBytesOfFile(File f) {
        try {
            InputStream is = new FileInputStream(testfile);
            long length = testfile.length();
            byte[] bytes = new byte[(int) length];
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Test
    public void testGetClassName_shouldWork() {
        byte[] bytes = getBytesOfFile(testfile);
        String string = service.getNameOfByteCode(bytes);
        assert string.equals("org.openengsb.experiments.weaver.internal.Activator");
    }

//    @Test
    public void testGetClass_shouldWork() {
        byte[] bytes = getBytesOfFile(testfile);
        Class<?> clazz = service.getClassOfByteCode(bytes);
        assert Activator.class.equals(clazz);
    }

    @Test
    public void testAddMethodLine_shouldWork() {
        byte[] bytes = getBytesOfFile(testfile);
        Activator activator = (Activator) service.addLogOutput(bytes, "getTestString");
        System.out.println(activator.getTestString());
    }
}

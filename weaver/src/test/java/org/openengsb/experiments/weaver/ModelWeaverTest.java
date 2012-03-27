package org.openengsb.experiments.weaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.experiments.provider.model.TestModel;
import org.openengsb.experiments.provider.model.TestModelObject;
import org.openengsb.experiments.weaver.internal.ModelWeaver;

public class ModelWeaverTest {

    private File testfile;
    private ModelWeaver service;

    @Before
    public void initiate() {
        testfile = new File("target/classes/org/openengsb/experiments/weaver/internal/Activator.class");
        service = new ModelWeaver();
    }

    private byte[] getBytesOfFile(File f) {
        try {
            InputStream is = new FileInputStream(f);
            long length = f.length();
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

    // @Test
    public void testGetClassName_shouldWork() {
        byte[] bytes = getBytesOfFile(testfile);
        String string = service.getNameOfByteCode(bytes);
        assert string.equals("org.openengsb.experiments.weaver.internal.Activator");
    }

//    @Test
    public void test() {
        File f = new File("target/test-classes/org/openengsb/experiments/weaver/TestObject2.class");
        byte[] bytes = getBytesOfFile(f);
        TestObject2 object = (TestObject2) service.appendInterfaceIfModelAnnotation(bytes);
        object.setName("blub");
        object.setId(42);
        for (TestModelObject obj : ((TestModel) object).getModelObjects()) {
            System.out.println(obj.getKey() + ":" + obj.getValue());
        }
        System.out.println("modelId=" + ((TestModel) object).getModelId());
    }

}

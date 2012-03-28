package org.openengsb.experiments.weaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.experiments.provider.model.TestModel;
import org.openengsb.experiments.provider.model.TestModelObject;
import org.openengsb.experiments.provider.util.ModelUtils;
import org.openengsb.experiments.weaver.internal.ModelWeaver;

public class ModelWeaverTest {
    private ModelWeaver service;

    @Before
    public void initiate() {
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

    @Test
    public void test() throws Exception {
        File f = new File("target/test-classes/org/openengsb/experiments/weaver/TestObject2.class");
        byte[] bytes = getBytesOfFile(f);
        TestObject2 object;
        object = (TestObject2) service.appendInterfaceIfModelAnnotation(bytes);
        object.setName("blub");
        object.setId(42);
        object.setFile(f);

        List<TestModelObject> objects = ModelUtils.getModelObjects(object);
        TestObject2 newObject = ModelUtils.createInstance(object.getClass(), objects);

        for (TestModelObject obj : objects) {
            System.out.println(obj.getKey() + ":" + obj.getValue());
        }

        System.out.println("newObject-name: " + newObject.getName());
        System.out.println("newObject-id: " + newObject.getId());
        System.out.println("newObject-file: " + newObject.getFile().getAbsolutePath());
    }
}

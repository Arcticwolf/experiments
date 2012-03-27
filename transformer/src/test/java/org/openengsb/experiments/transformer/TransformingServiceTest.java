package org.openengsb.experiments.transformer;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.experiments.transformer.internal.TransformingDescription;
import org.openengsb.experiments.transformer.internal.TransformingService;
import org.openengsb.experiments.transformer.models.ModelA;
import org.openengsb.experiments.transformer.models.ModelB;

public class TransformingServiceTest {
    private TransformingService service;

    @Before
    public void init() {
        service = new TransformingService();
    }

    @Test
    public void test1() {
        TransformingDescription desc = new TransformingDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.forwardField("blubA", "blubB");
        service.saveDescription(desc);

        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = service.performTransformation(ModelB.class, ModelA.class, model);
        assert result.getIdB().equals("test1");
        assert result.getTestB().equals("test2");
        assert result.getBlubB().equals("test3");
    }
    
    @Test
    public void test2() {
        TransformingDescription desc = new TransformingDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "testB");
        desc.forwardField("testA", "blubB");
        desc.forwardField("blubA", "idB");
        service.saveDescription(desc);
        
        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");

        ModelB result = service.performTransformation(ModelB.class, ModelA.class, model);
        assert result.getIdB().equals("test3");
        assert result.getTestB().equals("test1");
        assert result.getBlubB().equals("test2");
    }
    
    @Test
    public void test3() {
        TransformingDescription desc = new TransformingDescription(ModelA.class, ModelB.class);
        desc.forwardField("idA", "idB");
        desc.forwardField("testA", "testB");
        desc.concatField("blubB", "#", "blubA", "blaA");
        service.saveDescription(desc);
        
        ModelA model = new ModelA();
        model.setIdA("test1");
        model.setTestA("test2");
        model.setBlubA("test3");
        model.setBlaA("test4");

        ModelB result = service.performTransformation(ModelB.class, ModelA.class, model);
        assert result.getIdB().equals("test1");
        assert result.getTestB().equals("test2");
        assert result.getBlubB().equals("test3#test4");
    }
}

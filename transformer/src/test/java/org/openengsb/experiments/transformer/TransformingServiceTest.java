package org.openengsb.experiments.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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
        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3"));
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
        assertThat(result.getIdB(), is("test3"));
        assertThat(result.getTestB(), is("test1"));
        assertThat(result.getBlubB(), is("test2"));
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
        assertThat(result.getIdB(), is("test1"));
        assertThat(result.getTestB(), is("test2"));
        assertThat(result.getBlubB(), is("test3#test4"));
    }

    @Test
    public void test4() {
        TransformingDescription desc = new TransformingDescription(ModelB.class, ModelA.class);
        desc.forwardField("idB", "idA");
        desc.forwardField("testB", "testA");
        desc.splitField("blubB", "#", "blubA", "blaA");
        service.saveDescription(desc);

        ModelB model = new ModelB();
        model.setIdB("test1");
        model.setTestB("test2");
        model.setBlubB("test3#test4");

        ModelA result = service.performTransformation(ModelA.class, ModelB.class, model);
        assertThat(result.getIdA(), is("test1"));
        assertThat(result.getTestA(), is("test2"));
        assertThat(result.getBlubA(), is("test3"));
        assertThat(result.getBlaA(), is("test4"));
    }
}

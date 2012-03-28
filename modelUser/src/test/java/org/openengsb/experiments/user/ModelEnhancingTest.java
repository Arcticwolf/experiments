package org.openengsb.experiments.user;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.openengsb.experiments.provider.model.TestModel;
import org.openengsb.experiments.provider.model.TestModelObject;
import org.openengsb.experiments.provider.util.ModelUtils;

public class ModelEnhancingTest {

    @Test
    public void testIfModelIsTestModel_shouldWork() {
        TryOutModel model = new TryOutModel();
        assertThat(model instanceof TestModel, is(true));
    }

    @Test
    public void testModelGetIdWork_shouldWork() {
        TryOutModel object = new TryOutModel();
        object.setName("blub");
        object.setId(42);

        assertThat(ModelUtils.getModelId(object), is("42"));
    }

    @Test
    public void testSupportForFile_shouldWork() throws Exception {
        File f = new File("pom.xml");
        TryOutModel object = new TryOutModel();
        object.setName("blub");
        object.setId(42);
        object.setFile(f);

        List<TestModelObject> objects = ModelUtils.getModelObjects(object);
        TryOutModel newObject = ModelUtils.createInstance(object.getClass(), objects);

        assertThat(newObject.getName(), is(object.getName()));
        assertThat(newObject.getId(), is(object.getId()));
        assertThat(newObject.getFile().getAbsolutePath(), not(object.getFile().getAbsolutePath()));
    }
}

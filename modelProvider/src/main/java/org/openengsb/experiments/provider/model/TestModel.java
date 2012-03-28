package org.openengsb.experiments.provider.model;

import java.util.List;

public interface TestModel {
    
    List<TestModelObject> getModelObjects();

    String getModelId();
    
    Object createInstance(List<TestModelObject> objects);
}

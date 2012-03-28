package org.openengsb.experiments.provider.util;

import java.lang.reflect.Method;
import java.util.List;

import org.openengsb.experiments.provider.model.TestModel;
import org.openengsb.experiments.provider.model.TestModelObject;

public final class ModelUtils {

    private ModelUtils() {
    }

    public static String getModelId(Object model) {
        if (!(model instanceof TestModel)) {
            throw new IllegalArgumentException("Parameter must be a model");
        }
        TestModel test = (TestModel) model;
        return test.getModelId();
    }

    public static List<TestModelObject> getModelObjects(Object model) {
        if (!(model instanceof TestModel)) {
            throw new IllegalArgumentException("Parameter must be a model");
        }
        TestModel test = (TestModel) model;
        return test.getModelObjects();
    }

    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> modelType, List<TestModelObject> objects) {
        if (!TestModel.class.isAssignableFrom(modelType)) {
            throw new IllegalArgumentException("Parameter must be a model");
        }
        Object instance = null;
        try {
            instance = modelType.newInstance();
            for (TestModelObject tmo : objects) {
                String key = tmo.getKey();
                Object value = tmo.getValue();
                Class<?> type = tmo.getType();
                try {
                    String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
                    Method m = modelType.getMethod(setterName, type);
                    m.invoke(instance, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) instance;
    }

}

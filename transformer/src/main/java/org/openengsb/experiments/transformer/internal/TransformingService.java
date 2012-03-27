package org.openengsb.experiments.transformer.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TransformingService {
    private List<TransformingDescription> descriptions;

    public TransformingService() {
        descriptions = new ArrayList<TransformingDescription>();
    }

    public void saveDescription(TransformingDescription td) {
        for (TransformingDescription desc : descriptions) {
            if (desc.getSource().equals(td.getSource()) && desc.getTarget().equals(td.getTarget())) {
                descriptions.remove(desc);
                descriptions.add(td);
                return;
            }
        }
        descriptions.add(td);
    }

    public void deleteDescription(TransformingDescription td) {
        for (TransformingDescription desc : descriptions) {
            if (desc.getSource().equals(td.getSource()) && desc.getTarget().equals(td.getTarget())) {
                descriptions.remove(desc);
                return;
            }
        }
    }

    public <T> T performTransformation(Class<T> targetClass, Class<?> sourceClass, Object source) {
        try {
            T result = targetClass.newInstance();
            TransformingDescription desc = null;
            for (TransformingDescription td : descriptions) {
                if (td.getSource().equals(sourceClass) && td.getTarget().equals(targetClass)) {
                    desc = td;
                    break;
                }
            }
            if (desc != null) {
                for (TransformingStep step : desc.getTransformingSteps()) {
                    try {
                        switch (step.getOperation()) {
                            case FORWARD:
                                Method getter = sourceClass.getMethod(getGetterName(step.getSourceFields()[0]));
                                Object object = getter.invoke(source);
                                Method setter =
                                    targetClass.getMethod(getSetterName(step.getTargetField()), object.getClass());
                                setter.invoke(result, object);
                                break;
                            case CONCAT:
                                break;
                            default:
                                System.out.println("not supplied operation: " + step.getOperation());
                        }
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
                return result;
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("no description for this class pair defined");
    }

    private String getGetterName(String fieldname) {
        return "get" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }

    private String getSetterName(String fieldname) {
        return "set" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }
}

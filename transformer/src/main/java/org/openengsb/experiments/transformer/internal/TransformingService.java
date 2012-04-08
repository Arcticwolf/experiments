package org.openengsb.experiments.transformer.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class TransformingService {
    private List<TransformingDescription> descriptions;

    public TransformingService() {
        descriptions = new ArrayList<TransformingDescription>();
    }
    
    public List<TransformingDescription> getDescriptionsFromFile(File file) {
        List<TransformingDescription> desc = null;
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            MyXMLReader reader = new MyXMLReader();
            xr.setContentHandler(reader);
            xr.parse(file.getAbsolutePath());
            desc = reader.getResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return desc;
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

    @SuppressWarnings("unchecked")
    public <T> T performTransformation(Class<T> targetClass, Class<?> sourceClass, Object source) {
        try {
            TransformingDescription desc = null;
            for (TransformingDescription td : descriptions) {
                if (td.getSource().equals(sourceClass) && td.getTarget().equals(targetClass)) {
                    desc = td;
                    break;
                }
            }
            if (desc != null) {
                return (T) doActualTransformationSteps(desc, source);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("no description for this class pair defined");
    }

    private Object doActualTransformationSteps(TransformingDescription td, Object source)
        throws InstantiationException, IllegalAccessException {
        Object result = td.getTarget().newInstance();
        Method getter;
        Method setter;
        Object object;
        for (TransformingStep step : td.getTransformingSteps()) {
            try {
                switch (step.getOperation()) {
                    case FORWARD:
                        getter = td.getSource().getMethod(getGetterName(step.getSourceFields()[0]));
                        object = getter.invoke(source);
                        setter =
                            td.getTarget().getMethod(getSetterName(step.getTargetField()), object.getClass());
                        setter.invoke(result, object);
                        break;
                    case CONCAT:
                        StringBuilder builder = new StringBuilder();
                        for (String field : step.getSourceFields()) {
                            if (builder.length() != 0) {
                                builder.append(step.getOperationParam());
                            }
                            getter = td.getSource().getMethod(getGetterName(field));
                            builder.append(getter.invoke(source));
                        }
                        setter = td.getTarget().getMethod(getSetterName(step.getTargetField()), String.class);
                        setter.invoke(result, builder.toString());
                        break;
                    case SPLIT:
                        getter = td.getSource().getMethod(getGetterName(step.getTargetField()));
                        String split = (String) getter.invoke(source);
                        String[] splits = split.split(step.getOperationParam());
                        for (int i = 0; i < step.getSourceFields().length; i++) {
                            if (splits.length <= i) {
                                System.out.println("not enough split results for the target fields");
                                break;
                            }
                            String field = step.getSourceFields()[i];
                            setter = td.getTarget().getMethod(getSetterName(field), String.class);
                            setter.invoke(result, splits[i]);
                        }
                        if (splits.length > step.getSourceFields().length) {
                            System.out.println("too many split results for the target fields");
                        }
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

    private String getGetterName(String fieldname) {
        return "get" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }

    private String getSetterName(String fieldname) {
        return "set" + Character.toUpperCase(fieldname.charAt(0)) + fieldname.substring(1);
    }
}

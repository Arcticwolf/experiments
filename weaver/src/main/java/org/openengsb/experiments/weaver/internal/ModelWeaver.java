/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.experiments.weaver.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

import org.openengsb.experiments.provider.model.Model;
import org.openengsb.experiments.provider.model.ModelId;
import org.openengsb.experiments.provider.model.TestModel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

public class ModelWeaver implements WeavingHook {
    private ClassPool cp = ClassPool.getDefault();

    public ModelWeaver() {
        cp = ClassPool.getDefault();
        cp.importPackage("java.util");
        cp.importPackage("org.openengsb.experiments.provider.model");
    }

    public ModelWeaver(BundleContext context) {
        this();
        cp.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
    }

    @Override
    public void weave(WovenClass wovenClass) {
        String className = wovenClass.getClassName();
        if (className.equals("org.openengsb.experiments.provider.model.Model")
                || className.contains("javassist")) {
            return;
        }
        wovenClass.setBytes(extendModelInterface(wovenClass.getBytes()));
        try {
            CtClass clazz = cp.get(className);
            if (clazz != null) {
                clazz.defrost();
                clazz.detach();
                System.out.println(className + " got defrosted and detached");
            } else {
                System.out.println(className + " couldn't get defrosted and detached");
            }
        } catch (NotFoundException e) {
            System.out.println(className + " couldn't get defrosted and detached. Reason: not found");
            // ignore
        }
    }

    public String getNameOfByteCode(byte[] byteCode) {
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            return cc.getClassFile().getName();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (RuntimeException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    public byte[] extendModelInterface(byte[] byteCode) {
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            if (!hasAnnotation(cc, Model.class.getName())) {
                return byteCode;
            }
            System.out.println("Model to enhance: " + cc.getName());
            CtClass inter = cp.get(TestModel.class.getName());
            cc.addInterface(inter);

            CtMethod m = generateGetModelObjects(cc);
            cc.addMethod(m);
            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
            return cc.toBytecode();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (RuntimeException e1) {
            e1.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return byteCode;
    }

    private boolean hasAnnotation(CtClass clazz, String annotationName) {
        ClassFile cf = clazz.getClassFile2();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            cf.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)
            cf.getAttribute(AnnotationsAttribute.visibleTag);
        return checkAnnotation(ainfo, ainfo2, annotationName);
    }

    private boolean hasAnnotation(CtMethod method, String annotationName) {
        MethodInfo info = method.getMethodInfo();
        AnnotationsAttribute ainfo = (AnnotationsAttribute)
            info.getAttribute(AnnotationsAttribute.invisibleTag);
        AnnotationsAttribute ainfo2 = (AnnotationsAttribute)
            info.getAttribute(AnnotationsAttribute.visibleTag);
        return checkAnnotation(ainfo, ainfo2, annotationName);
    }

    private boolean checkAnnotation(AnnotationsAttribute invisible, AnnotationsAttribute visible,
            String annotationName) {
        boolean exist1 = false;
        boolean exist2 = false;
        if (invisible != null) {
            exist1 = invisible.getAnnotation(annotationName) != null;
        }
        if (visible != null) {
            exist2 = visible.getAnnotation(annotationName) != null;
        }
        return exist1 || exist2;
    }

    public Object appendInterfaceIfModelAnnotation(byte[] byteCode) {
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            if (cc.getAnnotation(Model.class) == null) {
                return cc.toClass().newInstance();
            }
            CtClass inter = cp.get(TestModel.class.getName());
            cc.addInterface(inter);

            CtMethod m = generateGetModelObjects(cc);
            cc.addMethod(m);
            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
            Class<?> clazz = cc.toClass();
            return clazz.newInstance();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (RuntimeException e1) {
            e1.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CtMethod generateGetModelObjects(CtClass clazz) throws NotFoundException,
        CannotCompileException, ClassNotFoundException {
        CtMethod m = new CtMethod(cp.get(List.class.getName()), "getModelObjects", new CtClass[]{}, clazz);

        StringBuilder builder = new StringBuilder();
        builder.append("{ \nList elements = new ArrayList();\n");
        for (CtMethod method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            String property = methodName.substring(3).toLowerCase();
            if (methodName.startsWith("get") && !methodName.equals("getModelObjects")) {
                builder.append("elements.add(new TestModelObject(\"");
                builder.append(property).append("\", ").append(methodName).append("(), ");
                builder.append(methodName).append("().getClass()));\n");
            }
            if (methodName.startsWith("set") && hasAnnotation(method, ModelId.class.getName())) {
                CtField field = new CtField(cp.get(String.class.getName()), "modelId", clazz);
                clazz.addField(field);
                method.insertAfter("modelId = \"\"+$1;");
                CtMethod idGetter = new CtMethod(cp.get(String.class.getName()), "getModelId", new CtClass[]{}, clazz);
                idGetter.setBody("{ return modelId; }");
                clazz.addMethod(idGetter);
            }
        }
        builder.append("return elements; } ");
        m.setBody(builder.toString());
        return m;
    }
}

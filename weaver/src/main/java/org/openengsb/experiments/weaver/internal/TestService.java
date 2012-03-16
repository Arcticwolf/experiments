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
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

import org.openengsb.experiments.provider.model.Model;
import org.openengsb.experiments.provider.model.TestModel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

public class TestService implements WeavingHook {
    private ClassPool cp = ClassPool.getDefault();

    public TestService() {
        cp = ClassPool.getDefault();
    }

    public TestService(BundleContext context) {
        cp = ClassPool.getDefault();
        cp.appendClassPath(new LoaderClassPath(context.getBundle().getClass().getClassLoader()));

        for (Bundle bundle : context.getBundles()) {
            if (bundle.getLocation().equals("mvn:org.openengsb.experiments/org.openengsb.experiments.provider/"
                    + "3.0.0-SNAPSHOT")) {
                Class<?> class1;
                try {
                    class1 = bundle.loadClass("org.openengsb.experiments.provider.model.Model");
                    cp.insertClassPath(new ClassClassPath(class1.getClass()));
                    class1 = bundle.loadClass("org.openengsb.experiments.provider.model.TestModel");
                    cp.insertClassPath(new ClassClassPath(class1.getClass()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public void weave(WovenClass wovenClass) {
        String className = wovenClass.getClassName();
        if (!className.contains("openengsb")
                || className.equals("org.openengsb.experiments.provider.model.Model")) {
            return;
        }
        System.out.println("Class to weave by weaver:\"" + className + "\"");
        wovenClass.setBytes(extendModelInterface(wovenClass.getBytes()));
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
            cp.importPackage("java.util");
            cp.importPackage("org.openengsb.experiments.provider.model");

            if (cc.getAnnotation(Model.class) == null) {
                return byteCode;
            }
            System.out.println("we have got a model to enhance :-)");
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

    public Object appendInterfaceIfModelAnnotation(byte[] byteCode) {
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            if (cc.getAnnotation(Model.class) == null) {
                return cc.toClass().newInstance();
            }
            CtClass inter = cp.get(TestModel.class.getName());
            cc.addInterface(inter);
            cp.importPackage("java.util");
            cp.importPackage("org.openengsb.experiments.provider.model");

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
        CannotCompileException {
        CtMethod m = new CtMethod(cp.get(List.class.getName()), "getModelObjects", new CtClass[]{}, clazz);

        StringBuilder builder = new StringBuilder();
        builder.append("{ \nList elements = new ArrayList();\n");
        for (CtMethod method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            String property = methodName.substring(3).toLowerCase();
            if (methodName.startsWith("get") && !methodName.equals("getModelObjects")) {
                builder.append("elements.add(new TestModelObject(").append("\"");
                builder.append(property).append("\", ").append(methodName).append("(), ");
                builder.append(methodName).append("().getClass()));\n");
            }
        }
        builder.append("return elements; } ");
        m.setBody(builder.toString());
        return m;
    }
}

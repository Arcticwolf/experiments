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

import org.openengsb.experiments.provider.model.Model;
import org.openengsb.experiments.provider.model.ModelId;
import org.openengsb.experiments.provider.model.TestModel;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;

public class ModelWeaver extends Weaver {

    @Override
    public byte[] doActualWeaving(byte[] byteCode) throws IOException, CannotCompileException {
        CtClass cc = doModelModifications(byteCode);
        return cc.toBytecode();
    }

    public Object appendInterfaceIfModelAnnotation(byte[] byteCode) throws InstantiationException,
        IllegalAccessException, CannotCompileException {
        CtClass cc = doModelModifications(byteCode);
        return cc.toClass().newInstance();
    }

    private CtClass doModelModifications(byte[] byteCode) {
        try {
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            if (!hasAnnotation(cc, Model.class.getName())) {
                return cc;
            }
            System.out.println("Model to enhance: " + cc.getName());
            CtClass inter = cp.get(TestModel.class.getName());
            cc.addInterface(inter);

            CtMethod m = generateGetModelObjects(cc);
            cc.addMethod(m);
            cc.setModifiers(cc.getModifiers() & ~Modifier.ABSTRACT);
            return cc;
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
        return null;
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

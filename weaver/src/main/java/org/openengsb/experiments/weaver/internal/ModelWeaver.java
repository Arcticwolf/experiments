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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.openengsb.experiments.provider.model.FileWrapper;
import org.openengsb.experiments.provider.model.Model;
import org.openengsb.experiments.provider.model.ModelId;
import org.openengsb.experiments.provider.model.TestModel;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

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
            if (!JavassistHelper.hasAnnotation(cc, Model.class.getName())) {
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

    private CtMethod generateGetModelObjects(CtClass clazz) throws NotFoundException,
        CannotCompileException, ClassNotFoundException {
        CtMethod m = new CtMethod(cp.get(List.class.getName()), "getModelObjects", new CtClass[]{}, clazz);

        StringBuilder builder = new StringBuilder();
        builder.append("{ \nList elements = new ArrayList();\n");
        for (CtMethod method : clazz.getDeclaredMethods()) {
            String methodName = method.getName();
            String property = JavassistHelper.generatePropertyName(methodName);
            if (methodName.startsWith("get") && !methodName.equals("getModelObjects")) {
                if (method.getReturnType().equals(cp.get(File.class.getName()))) {
                    String wrapperName = property + "wrapper";
                    builder.append("FileWrapper ").append(wrapperName).append(" = new FileWrapper(");
                    builder.append(methodName).append("());\n").append(wrapperName).append(".serialize();\n");
                    builder.append("elements.add(new TestModelObject(\"");
                    builder.append(wrapperName).append("\", ").append(wrapperName).append(", ");
                    builder.append(wrapperName).append(".getClass()));\n");
                    addFileFunction(clazz, property);
                } else {
                    builder.append("elements.add(new TestModelObject(\"");
                    builder.append(property).append("\", ").append(methodName).append("(), ");
                    builder.append(methodName).append("().getClass()));\n");
                }
            }
            if (methodName.startsWith("set") && JavassistHelper.hasAnnotation(method, ModelId.class.getName())) {
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

    private void addFileFunction(CtClass clazz, String property) throws NotFoundException, CannotCompileException {
        String wrapperName = property + "wrapper";
        String funcName = "set";
        funcName = funcName + Character.toUpperCase(wrapperName.charAt(0));
        funcName = funcName + wrapperName.substring(1);
        String setterName = "set";
        setterName = setterName + Character.toUpperCase(property.charAt(0));
        setterName = setterName + property.substring(1);
        CtClass[] params = new CtClass[]{ cp.get(FileWrapper.class.getName()) };
        CtMethod newFunc = new CtMethod(CtClass.voidType, funcName, params, clazz);
        newFunc.setBody("{ " + setterName + "($1.returnFile());\n }");
        clazz.addMethod(newFunc);
    }
}

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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

public class TestService implements WeavingHook {

    @Override
    public void weave(WovenClass wovenClass) {
        System.out.println("Class to weave by weaver:\"" + wovenClass.getClassName() + "\"");
        System.out.println("javassist says:\"" + getNameOfByteCode(wovenClass.getBytes()) + "\"");
    }

    public String getNameOfByteCode(byte[] byteCode) {
        try {
            ClassPool cp = ClassPool.getDefault();
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
    
    public Class<?> getClassOfByteCode(byte[] byteCode) {
        try {
            ClassPool cp = ClassPool.getDefault();
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            Class<?> clazz = cc.toClass();
            return clazz;
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (RuntimeException e1) {
            e1.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Object addLogOutput(byte[] byteCode, String methodName) {
        try {
            ClassPool cp = ClassPool.getDefault();
            InputStream stream = new ByteArrayInputStream(byteCode);
            CtClass cc = cp.makeClass(stream);
            CtMethod m = cc.getDeclaredMethod(methodName);
            m.insertBefore("System.out.println(\"blub\");");
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
        }
        return null;
    }

}

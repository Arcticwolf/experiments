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

import java.lang.reflect.Method;

import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

public class TestService implements WeavingHook {

    @Override
    public void weave(WovenClass wovenClass) {
        System.out.println("Class to weave by weaver:\"" + wovenClass.getClassName() + "\"");
        if (wovenClass.getClassName().equals("org.openengsb.experiments.provider.model.TestObject")) {
            Bundle bundle = wovenClass.getBundleWiring().getBundle();
            System.out.println(bundle.getSymbolicName());
            try {
                Class<?> clazz = wovenClass.getBundleWiring().getClassLoader().loadClass(wovenClass.getClassName());
                for (Method method : clazz.getMethods()) {
                    System.out.println(method.getName());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}

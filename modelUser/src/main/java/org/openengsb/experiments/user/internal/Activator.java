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

package org.openengsb.experiments.user.internal;

import org.openengsb.experiments.provider.model.TestModel;
import org.openengsb.experiments.provider.model.TestModelObject;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("Start User");
        Model2 object = new Model2();
        object.setId(42);
        object.setName("test");
        System.out.println("see if waving worked");
        TestModel model = (TestModel) object;
        System.out.println("got testmodel");
        for (TestModelObject obj : model.getModelObjects()) {
            System.out.println(obj.getKey() + ":" + obj.getValue());
        }
        System.out.println("Id of the model is : " + model.getModelId());
        System.out.println("User testcase ended");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
    }

}

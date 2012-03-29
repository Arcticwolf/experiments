package org.openengsb.experiments.weaver.internal;

import java.io.IOException;

import org.openengsb.experiments.provider.util.ManipulationUtils;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import javassist.CannotCompileException;

public abstract class Weaver implements WeavingHook {
    protected ManipulationUtils utils;
    
    public Weaver() {
        utils = ManipulationUtils.createInstance();
        utils.appendClassLoader(this.getClass().getClassLoader());
    }
    
    @Override
    public void weave(WovenClass wovenClass) {
        String className = wovenClass.getClassName();
        if (className.equals("org.openengsb.experiments.provider.model.Model")
                || className.contains("javassist") || className.contains("JavassistHelper")) {
            return;
        }
        try {
            wovenClass.setBytes(doActualWeaving(wovenClass.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }
    
    protected abstract byte[] doActualWeaving(byte[] wovenClass) throws IOException, CannotCompileException;
}

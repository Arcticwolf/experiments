package org.openengsb.experiments.weaver.internal;

import java.io.IOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

public abstract class Weaver implements WeavingHook {
    protected ClassPool cp = ClassPool.getDefault();
    
    public Weaver() {
        cp = ClassPool.getDefault();
        cp.importPackage("java.util");
        cp.importPackage("java.lang.reflect");
        cp.importPackage("org.openengsb.experiments.provider.model");
        cp.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
    }
    
    @Override
    public void weave(WovenClass wovenClass) {
        String className = wovenClass.getClassName();
        if (className.equals("org.openengsb.experiments.provider.model.Model")
                || className.contains("javassist")) {
            return;
        }
        try {
            wovenClass.setBytes(doActualWeaving(wovenClass.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        finishWeaving(className);
    }
    
    private void finishWeaving(String className) {
        try {
            CtClass clazz = cp.get(className);
            if (clazz != null) {
                clazz.defrost();
                clazz.detach();
            } else {
                System.out.println(className + " couldn't get defrosted and detached");
            }
        } catch (NotFoundException e) {
            System.out.println(className + " couldn't get defrosted and detached. Reason: not found");
            // ignore
        }
    }
    
    protected abstract byte[] doActualWeaving(byte[] wovenClass) throws IOException, CannotCompileException;
}

package com.zjn;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectClassVisitor extends ClassVisitor implements Opcodes {

    private String mClassName;

    public InjectClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        System.out.println("InjectClassVisitor : visit -----> started ：" + name);
        this.mClassName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.println("InjectClassVisitor : visitMethod : " + name);
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        //匹配FragmentActivity
        if ("com/jaca/codelib/MainActivity".equals(this.mClassName) && "onCreate".equals(name)) {
            //处理onCreate
            System.out.println("InjectClassVisitor : change method ----> " + name);
            return new InjectOnCreateMethodVisitor(mv);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        //System.out.println("LifecycleClassVisitor : visit -----> end");
        super.visitEnd();
    }
}

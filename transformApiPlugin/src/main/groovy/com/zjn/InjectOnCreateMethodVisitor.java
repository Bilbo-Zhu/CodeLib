package com.zjn;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class InjectOnCreateMethodVisitor extends MethodVisitor {

    public InjectOnCreateMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        //方法执行后插入
//        ALOAD 0
//        LDC "\u6211\u662f\u88ab\u63d2\u5165\u7684Toast"
//        ICONST_0
//        INVOKESTATIC android/widget/Toast.makeText (Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;
//        INVOKEVIRTUAL android/widget/Toast.show ()V
        if (opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC
                    , "com/jaca/codelib/MainActivityKt"
                    , "toast"
                    , "(Landroid/content/Context;)V"
                    , false);
//            mv.visitVarInsn(Opcodes.ALOAD, 0);
//            mv.visitLdcInsn("\u6211\u662f\u88ab\u63d2\u5165\u7684Toast");
//            mv.visitInsn(Opcodes.ICONST_0);
//            mv.visitMethodInsn(Opcodes.INVOKESTATIC
//                    , "android/widget/Toast"
//                    , "makeText"
//                    , "(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast"
//                    , false);
//            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL
//                    , "android/widget/Toast"
//                    , "show"
//                    , "()V"
//                    , false);
        }
        super.visitInsn(opcode);
    }
}

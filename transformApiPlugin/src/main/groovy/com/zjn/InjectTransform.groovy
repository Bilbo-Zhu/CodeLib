package com.zjn

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

/**
 * 定义一个Transform
 */
class InjectTransform extends Transform {

    private Project mProject

    // 构造函数，我们将Project保存下来备用
    InjectTransform(Project project) {
        this.mProject = project
    }

    // 设置我们自定义的Transform对应的Task名称
    // 类似：transformClassesWithPreDexForXXX
    // 这里应该是：transformClassesWithInjectTransformForxxx
    @Override
    String getName() {
        return 'InjectTransform'
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
    //  这样确保其他类型的文件不会传入
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    // 当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }


    /**
     * 检查class文件是否需要处理
     * @param fileName
     * @return
     */
    static boolean checkClassFile(String name) {
        //只处理需要的class文件
        return (name.endsWith(".class") && !name.startsWith("R\$")
                && !"R.class".equals(name) && !"BuildConfig.class".equals(name)
                && "MainActivity.class".equals(name))
    }

    // 核心方法
    // inputs是传过来的输入流，有两种格式：jar和目录格式
    // outputProvider 获取输出目录，将修改的文件复制到输出目录，必须执行
    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '--------------------transform 开始-------------------'

        // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
        inputs.each {
            TransformInput input ->
                // 遍历文件夹
                //文件夹里面包含的是我们手写的类以及R.class、BuildConfig.class以及R$XXX.class等
                input.directoryInputs.each { DirectoryInput directoryInput ->
                        // 注入代码
//                        InjectByJavassit.injectToast(directoryInput.file.absolutePath, mProject)

//                        // 获取输出目录
//                        def dest = outputProvider.getContentLocation(directoryInput.name,
//                                directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
//
////                        println("directory output dest: $dest.absolutePath")
//                        // 将input的目录复制到output指定目录
//                        FileUtils.copyDirectory(directoryInput.file, dest)

                    //是否是目录
                    if (directoryInput.file.isDirectory()) {
                        //列出目录所有文件（包含子文件夹，子文件夹内文件）
                        directoryInput.file.eachFileRecurse { File file ->
                            def name = file.name
                            if (checkClassFile(name)) {
                                println '----------- deal with "class" file <' + name + '> -----------'
                                ClassReader classReader = new ClassReader(file.readBytes())
                                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                                ClassVisitor cv = new InjectClassVisitor(classWriter)
                                classReader.accept(cv, EXPAND_FRAMES)
                                byte[] code = classWriter.toByteArray()
                                FileOutputStream fos = new FileOutputStream(
                                        file.parentFile.absolutePath + File.separator + name)
                                fos.write(code)
                                fos.close()
                            }
                        }
                    }
                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
                }

                //对类型为jar文件的input进行遍历
                input.jarInputs.each { JarInput jarInput ->
                        //jar文件一般是第三方依赖库jar文件
//                    JarInput jarInput ->
//                        // 重命名输出文件（同目录copyFile会冲突）
//                        def jarName = jarInput.name
//                        println '----------- deal with "jar" file before <' + jarName + '> -----------'
////                        println("jar: $jarInput.file.absolutePath")
//                        def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
//                        if (jarName.endsWith('.jar')) {
//                            jarName = jarName.substring(0, jarName.length() - 4)
//                        }
//                        def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//
//                        println("jar output dest: $dest.absolutePath")
//                        FileUtils.copyFile(jarInput.file, dest)

                    if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                        //重名名输出文件,因为可能同名,会覆盖
                        def jarName = jarInput.name
                        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }
                        JarFile jarFile = new JarFile(jarInput.file)
                        Enumeration enumeration = jarFile.entries()
                        File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
                        //避免上次的缓存被重复插入
                        if (tmpFile.exists()) {
                            tmpFile.delete()
                        }
                        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
                        //用于保存
                        while (enumeration.hasMoreElements()) {
                            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                            String entryName = jarEntry.getName()
                            ZipEntry zipEntry = new ZipEntry(entryName)
                            InputStream inputStream = jarFile.getInputStream(jarEntry)
//                            println '----------- deal with "jar" entryName with check <' + entryName + '> -----------'
                            //插桩class
                            if (checkClassFile(entryName)) {
                                //class文件处理
                                println '----------- deal with "jar" file <' + entryName + '> -----------'
                                jarOutputStream.putNextEntry(zipEntry)
                                ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                                ClassVisitor cv = new InjectClassVisitor(classWriter)
                                classReader.accept(cv, EXPAND_FRAMES)
                                byte[] code = classWriter.toByteArray()
                                jarOutputStream.write(code)
                            } else {
                                jarOutputStream.putNextEntry(zipEntry)
                                jarOutputStream.write(IOUtils.toByteArray(inputStream))
                            }
                            jarOutputStream.closeEntry()
                        }
                        //结束
                        jarOutputStream.close()
                        jarFile.close()
                        def dest = outputProvider.getContentLocation(jarName + md5Name,
                                jarInput.contentTypes, jarInput.scopes, Format.JAR)
                        org.apache.commons.io.FileUtils.copyFile(tmpFile, dest)
                        tmpFile.delete()
                    }
                }
        }

        println '---------------------transform 结束-------------------'
    }
}
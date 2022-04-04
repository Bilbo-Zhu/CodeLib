package com.zjn

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class TransformApiPlugin implements Plugin<Project> {
    void apply(Project project) {
        // 获取Android扩展
        def android = project.extensions.getByType(AppExtension)
        // 注册Transform，其实就是添加了Task
        android.registerTransform(new InjectTransform(project))
        project.task('hello1') {
            group = "zjn"
            description = "gradle build script demo"
            doLast {
                println "Hello from the com.zjn.TransformApiPlugin"
            }
        }
    }
}
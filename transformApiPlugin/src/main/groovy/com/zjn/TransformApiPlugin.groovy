package com.zjn

import org.gradle.api.Plugin
import org.gradle.api.Project

class TransformApiPlugin implements Plugin<Project> {
    void apply(Project project) {
        startApply()
        project.task('hello1') {
            group = "zjn"
            description = "gradle build script demo"
            doLast {
                println "Hello from the com.zjn.TransformApiPlugin"
            }
        }
    }

    private void startApply(){
        println '------------------------'
        println 'apply HelloPlugin'
        println '------------------------'
    }
}
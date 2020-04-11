/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mrsarm.jshell.plugin

import jdk.jshell.tool.JavaShellToolBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class JShellPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def jshellTask = project.tasks.create('jshell')
        jshellTask.group = 'application'
        jshellTask.description = 'Runs a JShell session with all the code and dependencies.'
        def classesTask = project.tasks.find { it.name == "classes" }
        if (classesTask) {
            jshellTask.dependsOn classesTask
        }
        jshellTask.doLast {
            if (!jshellTask.dependsOn) {
                // Some multi-module projects may not have the :classes task
                jshellTask.logger.warn ":jshell task :classes not found, be sure to compile the project first"
            }
            Set pathSet = []
            project.tasks.withType(JavaExec) {
                pathSet.addAll(classpath.findAll { it.exists() })
            }
            project.subprojects.each {
                it.tasks.withType(JavaExec) {
                    pathSet.addAll(classpath.findAll { it.exists() })
                }
            }
            Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader()) // promote class loader
            def path = pathSet.join(System.properties['os.name'].toLowerCase().contains('windows') ? ';' : ':')
            jshellTask.logger.info(":jshell executing with --class-path \"{}\"", path)
            String[] args = [
                "--class-path", path,
                "--startup", "DEFAULT",
                "--startup", "PRINTING"
            ]
            if (project.findProperty("jshell.startup") == "") {
                // Nothing, just avoid to run the startup.jsh if exists
            }
            else if (project.findProperty("jshell.startup")) {
                def jshellStartup = project.findProperty("jshell.startup")
                jshellTask.logger.info(":jshell executing with --startup DEFAULT --startup PRINTING " +
                                       "--startup \"{}\"", jshellStartup)
                args = args + (String[]) ["--startup", jshellStartup]
            }
            else {
                def startupJsh = new File("${project.projectDir}/startup.jsh")
                if (startupJsh.exists()) {
                    def startupJshPath = startupJsh.absolutePath
                    jshellTask.logger.info(":jshell executing with --startup DEFAULT --startup PRINTING" +
                                           " --startup \"{}\"", startupJshPath)
                    args = args + (String[]) ["--startup", startupJshPath]
                } else {
                    jshellTask.logger.info(":jshell did not find a startup.jsh script at the project dir " +
                                           "nor a `jshell.startup` configuration")
                }
            }
            JavaShellToolBuilder.builder().run(args)
        }
    }
}

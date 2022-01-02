/*
 * Copyright 2020-2022 the original author or authors.
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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec

class JShellPlugin implements Plugin<Project> {

    private shellArgs = []

    // This task could be merged with the main task in just one,
    // the problem is that the main task is a JavaExec one,
    // and at least for now I couldn't find a way
    // to get the dependencies list with this type
    private Task createJshellSetupTask(Project project) {
        Task jshellTask = project.tasks.create('jshellSetup')
        def classesTask = project.tasks.find { it.name == "classes" }
        if (classesTask) {
            jshellTask.logger.info 'Task "classes" found.'
            jshellTask.dependsOn classesTask
        } else {
            jshellTask.logger.warn 'Task "classes" NOT found.'
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
            if (pathSet.isEmpty()) {
                List<String> classesPaths = project.sourceSets.main.output.getClassesDirs().collect { it.getPath() }
                jshellTask.logger.info(":jshell couldn't find the classpath, adding " +
                                       'the following paths from project sourceSets: {}', classesPaths)
                pathSet.addAll(classesPaths)
                List<String> depsPaths = project.configurations.default.collect { it.getPath() }
                jshellTask.logger.info(":jshell couldn't find the dependencies' classpath, adding " +
                                       'the following paths from project configurations: {}', depsPaths)
                pathSet.addAll(depsPaths)
            }
            def path = pathSet.join(System.getProperty("path.separator"))
            jshellTask.logger.info(":jshell executing with --class-path {}", path)
            shellArgs += [
                "--class-path", path,
                "--startup", "DEFAULT",
                "--startup", "PRINTING"
            ]
            if (project.findProperty("jshell.startup") == "") {
                jshellTask.logger.info ':jshell "jshell.startup" set to empty, skipping "startup.jsh" execution'
            }
            else if (project.findProperty("jshell.startup")) {
                def jshellStartup = project.findProperty("jshell.startup")
                jshellTask.logger.info(":jshell executing with --startup DEFAULT --startup PRINTING " +
                                       "--startup \"{}\"", jshellStartup)
                shellArgs += ["--startup", jshellStartup]
            }
            else {
                def startupJsh = new File("${project.projectDir}/startup.jsh")
                if (startupJsh.exists()) {
                    def startupJshPath = startupJsh.absolutePath
                    jshellTask.logger.info(":jshell executing with --startup DEFAULT --startup PRINTING" +
                                           " --startup \"{}\"", startupJshPath)
                    shellArgs += ["--startup", startupJshPath]
                } else {
                    jshellTask.logger.info(":jshell did not find a startup.jsh script at the project dir " +
                                           "nor a `jshell.startup` configuration")
                }
            }
        }
        return jshellTask
    }

    @Override
    void apply(Project project) {
        Task jshellSetupTask = createJshellSetupTask(project)
        project.tasks.register("jshell", Exec) {
            group = 'application'
            description = 'Runs a JShell session with all the code and dependencies.'
            dependsOn jshellSetupTask
            doFirst {
                standardInput = System.in
                executable = "jshell"
                args(shellArgs)
            }
        }
    }
}

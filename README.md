JShell Plugin
=============

This Gradle plugin helps you to explore your code and dependencies in your gradle project
with in [jshell](https://docs.oracle.com/javase/9/jshell/introduction-jshell.htm),
the official Java REPL tool.

Hosted on (NOT published yet): https://plugins.gradle.org/plugin/com.github.mrsarm.jshell.plugin


Getting started
---------------

To use this plugin, add the following to your `build.gradle`:

```groovy
plugins {
  id "com.github.mrsarm.jshell.plugin" version "1.0.0-RC1"
}
```

or for Gradle < 2.1:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.mrsarm:jshell-plugin:1.0.0-RC1"
  }
}

apply plugin: "com.github.mrsarm.jshell.plugin"
```

Task `jshell` is now enabled, which execute jshell with your classes and
dependencies after compiling your code.

You need to run the task `jshell` with the options `--no-daemon --console plain`.
Following is an example:

    $ gradle --no-daemon --console plain jshell

If you see this warning and the jshell console does not detect your classes:

> :jshell task :classes not found, be sure to compile the project first

Means the `classes` task needed to compile your project before launch `jshell`
does not exist, just append the task needed to compile the project,
some times is the same `classes` task but is not detected in multi-modules
projects, so you need to add it explicitly in the Gradle command:

    $ gradle --no-daemon --console plain classes jshell


System Requirements
-------------------

 * JDK 9+
 * Gradle


Build & Publish
---------------

Compile and build the .jar locally with:

    $ ./gradlew build

Publish to your local Maven repo:

    $ ./gradlew publishToMavenLocal

About
-----

This is a fork of the project https://github.com/bitterfox/jshell-gradle-plugin ,
I forked it because the original project is not receiving patches
and this version solve some issues and adds the following features:

 - It works with **multi-module projects**
 - There is no need to set the env variable `JAVA_OPTS` with a bunch of
   of arguments _"--add-exports jdk.jshell/jdk.intern..."_
 - _Coming soon_: add special support to the **Spring Framework**

Project: https://github.com/mrsarm/jshell-plugin

Authors:

 - Mariano Ruiz <mrsarm@gmail.com>
 - https://github.com/bitterfox
   (original [project](https://github.com/bitterfox/jshell-gradle-plugin))

License: (2020) [Apache Software License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

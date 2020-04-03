JShell Plugin
=============

This **Gradle plugin** helps you to explore your code and dependencies in your gradle project
with in [jshell](https://docs.oracle.com/javase/9/jshell/introduction-jshell.htm),
the official Java REPL tool.

Hosted on _(release candidate)_: https://plugins.gradle.org/plugin/com.github.mrsarm.jshell.plugin

 - [Getting started](#getting-started)
 - [Startup options](#startup-options)
 - [Troubleshooting](#troubleshooting)
 - [System Requirements](#system-requirements)
 - [Build and Publish](#build-an-publish)
 - [About](#about)


Getting started
---------------

To use this plugin, add the following to your `build.gradle`:

```groovy
plugins {
  id "com.github.mrsarm.jshell.plugin" version "1.0.0-RC1"
}
```

or in Gradle < 2.1:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.com.github.mrsarm:jshell-plugin:1.0.0-RC1"
  }
}

apply plugin: "com.github.mrsarm.jshell.plugin"
```

Task `jshell` is now enabled, which execute jshell with your classes and
dependencies after compiling your code.

You need to run the task `jshell` with the options `--no-daemon --console plain`.
Following is an example:

    $ gradle --no-daemon --console plain jshell

Startup options
---------------

The shell console starts with the options
`--startup DEFAULT --startup PRINTING` so always
the following imports and functions are available:

 - java.io
 - java.math
 - java.net
 - java.nio.file
 - java.util
 - java.util.concurrent
 - java.util.function
 - java.util.prefs
 - java.util.regex
 - java.util.stream
 - print (alias of `System.out.print`)
 - println (alias of `System.out.println`)

If the plugin founds at the root of the project a
[JShell Script](https://docs.oracle.com/javase/9/jshell/scripts.htm)
named `startup.jsh`, it will append to the JShell session
the argument `--startup startup.jsh`, executing
at the beginning all the instruction in the script,
so you can add there all the imports, object definitions
or any Java instruction that you want to execute
at the begging of the session. You can override
the startup script path with the project property
`jshell.startup` in the `gradle.properties` file,
or set the same property in the command line
arguments, like: 

    $ gradle --no-daemon --console plain jshell -Pjshell.startup=/path/to/run.jsh


Troubleshooting
---------------

### JShell warning at startup

If you see this warning and the jshell console does not detect your classes:

> :jshell task :classes not found, be sure to compile the project first

Means the `classes` task needed to compile your project before launch `jshell`
does not exist, just append in the command line the task needed to compile
the project, some times is the same `classes` task but is not detected
in multi-modules projects, so you need to add it explicitly in the
Gradle command:

    $ gradle --no-daemon --console plain classes jshell

### I have a JDK 9+ installation but my default JDK is the JDK 8 or below

In that case Gradle will try to use the default JDK, and `jshell` is
not available in Java 8 and above. Moreover the steps to change the
default JDK vary depending of your system, but Gradle use the default
JDK except if it's defined the `$JAVA_HOME` environment variable,
so when you enter in a new console where you need to use your Java 9+
installation with Gradle, just export the variable with the path to
the JDK installation, eg:

    $ export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"

The export will live just whiting the session where is defined,
it does not change your system configuration globally, and calling
to `unset JAVA_HOME` or opening a new session the export will
not have effect anymore.

You can even create an alias in your `~/.profile` / `~/.bashrc`
file like: `alias setjava9='export JAVA_HOME=/System/Library/Java/...'`
to later switch easily to the other distribution calling
`setjava9`.


System Requirements
-------------------

 * JDK 9+
 * Gradle


Build and Publish
-----------------

Compile and build the .jar locally with:

    $ ./gradlew build

Publish to your local Maven repo:

    $ ./gradlew publishToMavenLocal

Publish to [plugins.gradle.org](https://plugins.gradle.org/):

    $ ./gradlew publishPlugins


About
-----

This is a fork of the project https://github.com/bitterfox/jshell-gradle-plugin ,
I forked it because the original project is not receiving patches
and this version solves some issues and adds the following features:

 - It works with **multi-module projects**
 - There is no need to set the env variable `JAVA_OPTS` with a bunch of
   of arguments _"--add-exports jdk.jshell/jdk.intern..."_
 - It allows to run at the beginning of the session a _.jsh_ startup script
 - _Coming soon_: special support to the **Spring Framework**

Project: https://github.com/mrsarm/jshell-plugin

### Authors

 - Mariano Ruiz <mrsarm@gmail.com>
 - https://github.com/bitterfox
   (original [project](https://github.com/bitterfox/jshell-gradle-plugin))

### License

 - (2020) [Apache Software License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

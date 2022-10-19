JShell Plugin
=============

**Gradle plugin** that helps you to explore your code and dependencies in your gradle project
with in [jshell](https://docs.oracle.com/javase/9/jshell/introduction-jshell.htm),
the official Java REPL tool (Java 9+).

Hosted in the **Gradle plugins** site: https://plugins.gradle.org/plugin/com.github.mrsarm.jshell.plugin

 - [Getting started](#getting-started)
 - [Startup options](#startup-options)
 - [Spring Boot applications](#spring-boot-applications)
 - [Troubleshooting](#troubleshooting)
 - [System Requirements](#system-requirements)
 - [Build and Publish](#build-and-publish)
 - [About](#about)


Getting started
---------------

To use this plugin, add the following to your `build.gradle`:

```groovy
plugins {
  id "com.github.mrsarm.jshell.plugin" version "1.2.0"
}
```

Task `jshell` is now enabled, which execute the jshell with your classes and
dependencies after compiling your code.

You need to run the task `jshell` with the option `--console plain`.
Following is an example:

    $ gradle --console plain jshell
    
    ... (gradle tasks output) ...
    
    > Task :jshell
    |  Welcome to JShell -- Version 11.0.6
    |  For an introduction type: /help intro
    
    jshell> println("Hello world!")
    Hello world!
    
    jshell> _


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

### JShell scripts

If the plugin founds at the root of the project a
[JShell Script](https://docs.oracle.com/javase/9/jshell/scripts.htm)
named `startup.jsh`, it will append to the JShell session
the argument `--startup startup.jsh`, executing
at the beginning all the instructions in the script,
so you can add there all the imports, object definitions
or any Java instruction that you want to execute
at the beginning of the session.

You can override the startup script path with the project
property `jshell.startup` in the `gradle.properties` file,
or set the same property directly in the command line
arguments:

    $ gradle --console plain jshell -Pjshell.startup=/path/to/run.jsh

If you have a `startup.jsh` script at the root of the project
but at some point you don't want to execute it nor any other
startup script within the session, just pass the `jshell.startup` property
with an empty value: `gradle --console plain jshell -Pjshell.startup=`.


Spring Boot applications
------------------------

The JShell plugin allows to startup a Spring Boot application
within the console and access to the business object from there,
but you will need to do some extra configurations, and add
a dependency to the project to make easier to access
to the Spring beans.

1. Setup the plugin following the steps in the
   [Getting started](#getting-started) section.

2. Add the library [spring-ctx](https://github.com/mrsarm/spring-ctx)
   to your dependencies as follows:

   - Add the following dependency to your `dependencies` section:
     
     ```groovy
     implementation 'com.github.mrsarm:spring-ctx:1.0.0'
     ```

   - At the end of the `repositories` section:

     ```groovy
     maven { url 'https://jitpack.io' }
     ```

3. Any Spring Boot application has a class annotated with
   `@SpringBootApplication` that is the starting point of
   the application, with a `public static void main(String[] args)`
   method, you need to create a [startup.jsh](#startup-options) file
   at the root of your project calling that method, eg:

   ```java
   com.my.package.MyApplication.main(new String[]{})
   ```

   You can also add the imports of the business classes you are going
   to play with, as many as you have, otherwise you can import them
   once the JShell started:

   ```java
   import com.my.package.services.MyUserService
   ```

4. Done. You can start playing with your Spring application, you can
   access to the bean objects once the JShell started as
   following:

   ```
   $ ./gradlew --console plain jshell
   
     .   ____          _            __ _ _
    /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
   ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
    \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
     '  |____| .__|_| |_|_| |_\__, | / / / /
    =========|_|==============|___/=/_/_/_/
    :: Spring Boot ::        (v2.2.4.RELEASE)
   
   14:43:28.320  INFO com.my.package.MyApplication             : Starting Application on kubu1910 with PID 5225 (/projects/...
   14:43:28.323 DEBUG com.my.package.MyApplication             : Running with Spring Boot v2.2.4.RELEASE, Spring v5.2.3.RELEASE
   14:43:28.324  INFO com.my.package.MyApplication             : The following profiles are active: ...
   14:43:30.229  INFO boot.web.embedded.tomcat.TomcatWebServer : Tomcat initialized with port(s): 8010 (http)
   
   ...
   ...
   
   14:43:33.729  INFO tuate.endpoint.web.EndpointLinksResolver : Exposing 3 endpoint(s) beneath base path ''
   14:43:33.811  INFO boot.web.embedded.tomcat.TomcatWebServer : Tomcat started on port(s): 8010 (http) with context path ''
   14:43:33.816  INFO com.my.package.MyApplication             : Started Application in 6.995 seconds (JVM running for 10.524)
   
   > Task :jshell
   |  Welcome to JShell -- Version 11.0.6
   |  For an introduction type: /help intro
   
   jshell> var myUserService = ctx.App.getBean(MyUserService.class)
   
   jshell> ctx.App.ppjson(myUserService.getByUsername("admin"))
   {
     "name" : "Jhon",
     "lastName" : "Due",
     "username" : "admin",
     "age" : null
   }
   ```

   You can add to the `startup.jsh` not just the call to the `main` method
   and useful imports, but also adds the bean declarations you are
   going to use most likely, eg.
   `var myUserService = ctx.App.getBean(MyUserService.class)`, or any other
   snippet of Java code that may save you time running each time the JShell.

   The class `ctx.App` comes from the `spring-ctx` dependency added, checkout the
   [documentation](https://github.com/mrsarm/spring-ctx/blob/master/README.md)
   of all the useful methods it has to help you to play with
   the Spring framework.


Troubleshooting
---------------

### JShell warning at startup

If you see this warning before the jshell console starts:

> :jshell task :classes not found, be sure to compile the project first

Means the `classes` task needed to compile your project before launch `jshell`
does not exist, just append in the command line the task needed to compile
the project. Some times is the same `classes` task but is not detected
in multi-modules projects, so you need to add it explicitly in the
Gradle command:

    $ gradle --console plain classes jshell

Later if you need to run again the JShell but no change to the
source code was performed, you don't need to append the `classes` task,
although the warning will still appear, just ignore it.

### The console is exited once started

Currently, there is an [issue#1](https://github.com/mrsarm/jshell-plugin/issues/1)
(and [issue#4](https://github.com/mrsarm/jshell-plugin/issues/4)) that
the plugin does not work properly with Java 12 and above, exiting
the interactive console as following:

    $ ./gradlew --console plain jshell
    > Task :compileJava UP-TO-DATE
    > Task :processResources NO-SOURCE
    > Task :classes UP-TO-DATE
    
    > Task :jshellSetup
    
    > Task :jshell
    |  Welcome to JShell -- Version 12.0.2
    |  For an introduction type: /help intro
    
    BUILD SUCCESSFUL in 1s
    3 actionable tasks: 2 executed, 1 up-to-date

    $ _

It's recommended to **use the latest LTS: version 11**, but it works
with Java 9 and 10 as well. Previous versions (Java 8, 7...) are not
supported because the JShell is not built-in on those versions, but
if you want to run the JShell with an old project, pay special attention
to the section bellow.

Although if you cannot run with a version bellow to Java 12, you are
still able to run [JShell scripts](#jshell-scripts) with the plugin,
but once executed the _.jsh_ script the interactive console will exit.

### I have a JDK 9+ installation, but my default JDK is the JDK 8 or below

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
`unset JAVA_HOME` or opening a new session the export will
not have effect anymore.

You can even create an alias in your `~/.profile` / `~/.bashrc`
file like: `alias usejava9='export JAVA_HOME=/System/Library/Java/...'`
to later switch easily to the other distribution calling
`usejava9`, or `usejava8` to switch back.

Another good alternative to install and switch between different
versions of Java is [SdkMan!](https://sdkman.io/), totally recommended,
and it also allows to install multiple versions of Gradle, Maven, Kotlin...
and many Java based tools.

### Gradle output is mixed with the jshell output in the console

If content like `<-------------> 0% EXECUTING [16s]` is mixed
in the console with the jshell output each time you try
to execute something within the jshell console, remember to pass
the option `--console plain` to the gradle command, but if
the output continues mixed up with Gradle messages, try
adding the option `--no-daemon` to start up the jshell:

    $ gradle --no-daemon --console plain jshell

### Tab completion not working

Auto-completion does not work, and pressing arrow keys triggers
weird characters in the console. Unfortunately, with the
current console support from Gradle, there is no
solution for this [bug](https://github.com/mrsarm/jshell-plugin/issues/2).

However, the JShell task can be invoked with the command **rlwrap** from the
console to overcome the issue:

    $ rlwrap ./gradlew --console plain jshell

`rlwrap` takes care of the autocompletion, and the arrow keys management.
Just remember to check you have installed it before invoking the JShell with
the command the first time.

You can install [rlwrap](https://github.com/hanslub42/rlwrap) on Windows using
Cygwin or MinGW, and on Linux and MacOS.  For MacOS, consider installing with
Homebrew: `brew install rlwrap`

System Requirements
-------------------

 * JDK 9+
 * Gradle


Build and Publish
-----------------

> :information_source: Only for developers that want to make
> changes to this source code.

To compile this plugin and build the .jar locally, checkout the
source code and execute:

    $ ./gradlew build

Publish to your local Maven repo (normally `~/.m2/repository/`):

    $ ./gradlew publishToMavenLocal

Publish to [plugins.gradle.org](https://plugins.gradle.org/):

    $ ./gradlew publishPlugins

### Using from the local repo

To test local changes published to the local repo with the
`publishToMavenLocal` task mentioned above, you need
to add the plugin to the `build.gradle` in the project
where the plugin is tested, in the following manner: 

```groovy
buildscript {
  repositories {
    mavenLocal()
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.mrsarm:jshell-plugin:1.2.0"
  }
}

apply plugin: "com.github.mrsarm.jshell.plugin"
```

Also, when launching the plugin from another project, to see the ":jshell" logs
add the `--info` argument in the command line, e.g.: `gradle --console plain jshell --info`.

About
-----

This is a fork of the project https://github.com/bitterfox/jshell-gradle-plugin ,
I forked it because the original project is not receiving patches
and this version solves some issues and adds the following features:

 - It works with **multi-module projects**.
 - There is no need to set the env variable `JAVA_OPTS` with a bunch
   of arguments _"--add-exports jdk.jshell/jdk.intern..."_.
 - It allows to run at the beginning of the session a _.jsh_ startup script.
 - Special support to the **Spring Framework**.

**Project**: https://github.com/mrsarm/jshell-plugin

### Authors

 - Mariano Ruiz <mrsarm@gmail.com>
 - https://github.com/bitterfox
   (original [project](https://github.com/bitterfox/jshell-gradle-plugin))

### License

 - (2020-2022) [Apache Software License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

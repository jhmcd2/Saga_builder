To honour the JVM settings for this build a single-use Daemon process will be forked. See https://docs.gradle.org/7.3.3/userguide/gradle_daemon.html#sec:disabling_the_daemon.
Daemon will be stopped at the end of the build 

> Task :core:silkwave-connector:dependencies

------------------------------------------------------------
Project ':core:silkwave-connector'
------------------------------------------------------------

annotationProcessor - Annotation processors and their dependencies for source set 'main'.
No dependencies

api - API dependencies for source set 'main'. (n)
\--- project core-common (n)

apiElements - API elements for main. (n)
No dependencies

archives - Configuration for archive artifacts. (n)
No dependencies

compileClasspath - Compile classpath for source set 'main'.
\--- project :core:core-common
     +--- project :core:silkwave-intf
     |    \--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
     |         \--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- com.google.code.gson:gson:2.10.1
     +--- com.google.guava:guava:31.1-jre
     |    +--- com.google.guava:failureaccess:1.0.1
     |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     |    +--- com.google.code.findbugs:jsr305:3.0.2
     |    +--- org.checkerframework:checker-qual:3.12.0
     |    +--- com.google.errorprone:error_prone_annotations:2.11.0
     |    \--- com.google.j2objc:j2objc-annotations:1.3
     +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
     |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
     |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     |         +--- jakarta.activation:jakarta.activation-api:2.1.3
     |         +--- org.glassfish.jaxb:txw2:4.0.5
     |         \--- com.sun.istack:istack-commons-runtime:4.1.2
     +--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
     +--- org.apache.activemq:activemq-client:6.1.3
     |    +--- org.slf4j:slf4j-api:2.0.13
     |    +--- jakarta.jms:jakarta.jms-api:3.1.0
     |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
     +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
     |    +--- org.apache.logging.log4j:log4j-api:2.22.1
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.apache.logging.log4j:log4j-core:2.22.1
     |    \--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
     |    +--- javax.xml.stream:stax-api:1.0-2
     |    \--- org.codehaus.woodstox:stax2-api:3.0.2
     |         \--- javax.xml.stream:stax-api:1.0-2
     +--- org.eclipse.jetty:jetty-client:11.0.24
     |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
     |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
     |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-http:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     \--- org.eclipse.jetty:jetty-servlet:11.0.24
          +--- org.eclipse.jetty:jetty-security:11.0.24
          |    +--- org.eclipse.jetty:jetty-server:11.0.24
          |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
          |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
          |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
          |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
          |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
          \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13

compileOnly - Compile only dependencies for source set 'main'. (n)
No dependencies

compileOnlyApi - Compile only API dependencies for source set 'main'. (n)
No dependencies

default - Configuration for default artifacts. (n)
No dependencies

implementation - Implementation only dependencies for source set 'main'. (n)
No dependencies

jacocoAgent - The Jacoco agent to use to get coverage data.
\--- org.jacoco:org.jacoco.agent:0.8.7

jacocoAnt - The Jacoco ant tasks to use to get execute Gradle tasks.
\--- org.jacoco:org.jacoco.ant:0.8.7
     +--- org.jacoco:org.jacoco.core:0.8.7
     |    +--- org.ow2.asm:asm:9.1
     |    +--- org.ow2.asm:asm-commons:9.1
     |    |    +--- org.ow2.asm:asm:9.1
     |    |    +--- org.ow2.asm:asm-tree:9.1
     |    |    |    \--- org.ow2.asm:asm:9.1
     |    |    \--- org.ow2.asm:asm-analysis:9.1
     |    |         \--- org.ow2.asm:asm-tree:9.1 (*)
     |    \--- org.ow2.asm:asm-tree:9.1 (*)
     +--- org.jacoco:org.jacoco.report:0.8.7
     |    \--- org.jacoco:org.jacoco.core:0.8.7 (*)
     \--- org.jacoco:org.jacoco.agent:0.8.7

runtimeClasspath - Runtime classpath of source set 'main'.
\--- project :core:core-common
     +--- project :core:silkwave-intf
     |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
     |    |    \--- jakarta.activation:jakarta.activation-api:2.1.3
     |    \--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- com.google.code.gson:gson:2.10.1
     +--- com.google.guava:guava:31.1-jre
     |    +--- com.google.guava:failureaccess:1.0.1
     |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     |    +--- com.google.code.findbugs:jsr305:3.0.2
     |    +--- org.checkerframework:checker-qual:3.12.0
     |    +--- com.google.errorprone:error_prone_annotations:2.11.0
     |    \--- com.google.j2objc:j2objc-annotations:1.3
     +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
     |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
     |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     |         +--- jakarta.activation:jakarta.activation-api:2.1.3
     |         +--- org.eclipse.angus:angus-activation:2.0.2
     |         |    \--- jakarta.activation:jakarta.activation-api:2.1.3
     |         +--- org.glassfish.jaxb:txw2:4.0.5
     |         \--- com.sun.istack:istack-commons-runtime:4.1.2
     +--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
     +--- org.apache.activemq:activemq-client:6.1.3
     |    +--- org.slf4j:slf4j-api:2.0.13
     |    +--- jakarta.jms:jakarta.jms-api:3.1.0
     |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
     +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
     |    +--- org.apache.logging.log4j:log4j-api:2.22.1
     |    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    \--- org.apache.logging.log4j:log4j-core:2.22.1
     |         \--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
     +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
     |    +--- javax.xml.stream:stax-api:1.0-2
     |    \--- org.codehaus.woodstox:stax2-api:3.0.2
     |         \--- javax.xml.stream:stax-api:1.0-2
     +--- org.eclipse.jetty:jetty-client:11.0.24
     |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
     |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
     |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-http:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     +--- org.eclipse.jetty:jetty-servlet:11.0.24
     |    +--- org.eclipse.jetty:jetty-security:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-server:11.0.24
     |    |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
     |    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
     |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     \--- xpp3:xpp3:1.1.4c

runtimeElements - Elements of runtime for main. (n)
No dependencies

runtimeOnly - Runtime only dependencies for source set 'main'. (n)
No dependencies

testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
No dependencies

testCompileClasspath - Compile classpath for source set 'test'.
\--- project :core:core-common
     +--- project :core:silkwave-intf
     |    \--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
     |         \--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- com.google.code.gson:gson:2.10.1
     +--- com.google.guava:guava:31.1-jre
     |    +--- com.google.guava:failureaccess:1.0.1
     |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     |    +--- com.google.code.findbugs:jsr305:3.0.2
     |    +--- org.checkerframework:checker-qual:3.12.0
     |    +--- com.google.errorprone:error_prone_annotations:2.11.0
     |    \--- com.google.j2objc:j2objc-annotations:1.3
     +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
     |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
     |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     |         +--- jakarta.activation:jakarta.activation-api:2.1.3
     |         +--- org.glassfish.jaxb:txw2:4.0.5
     |         \--- com.sun.istack:istack-commons-runtime:4.1.2
     +--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
     +--- org.apache.activemq:activemq-client:6.1.3
     |    +--- org.slf4j:slf4j-api:2.0.13
     |    +--- jakarta.jms:jakarta.jms-api:3.1.0
     |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
     +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
     |    +--- org.apache.logging.log4j:log4j-api:2.22.1
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.apache.logging.log4j:log4j-core:2.22.1
     |    \--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
     |    +--- javax.xml.stream:stax-api:1.0-2
     |    \--- org.codehaus.woodstox:stax2-api:3.0.2
     |         \--- javax.xml.stream:stax-api:1.0-2
     +--- org.eclipse.jetty:jetty-client:11.0.24
     |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
     |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
     |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-http:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     \--- org.eclipse.jetty:jetty-servlet:11.0.24
          +--- org.eclipse.jetty:jetty-security:11.0.24
          |    +--- org.eclipse.jetty:jetty-server:11.0.24
          |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
          |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
          |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
          |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
          |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
          \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13

testCompileOnly - Compile only dependencies for source set 'test'. (n)
No dependencies

testImplementation - Implementation only dependencies for source set 'test'. (n)
No dependencies

testRuntimeClasspath - Runtime classpath of source set 'test'.
\--- project :core:core-common
     +--- project :core:silkwave-intf
     |    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
     |    |    \--- jakarta.activation:jakarta.activation-api:2.1.3
     |    \--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- com.google.code.gson:gson:2.10.1
     +--- com.google.guava:guava:31.1-jre
     |    +--- com.google.guava:failureaccess:1.0.1
     |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
     |    +--- com.google.code.findbugs:jsr305:3.0.2
     |    +--- org.checkerframework:checker-qual:3.12.0
     |    +--- com.google.errorprone:error_prone_annotations:2.11.0
     |    \--- com.google.j2objc:j2objc-annotations:1.3
     +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
     |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
     |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     |         +--- jakarta.activation:jakarta.activation-api:2.1.3
     |         +--- org.eclipse.angus:angus-activation:2.0.2
     |         |    \--- jakarta.activation:jakarta.activation-api:2.1.3
     |         +--- org.glassfish.jaxb:txw2:4.0.5
     |         \--- com.sun.istack:istack-commons-runtime:4.1.2
     +--- jakarta.activation:jakarta.activation-api:2.1.3
     +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
     +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
     +--- org.apache.activemq:activemq-client:6.1.3
     |    +--- org.slf4j:slf4j-api:2.0.13
     |    +--- jakarta.jms:jakarta.jms-api:3.1.0
     |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
     +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
     |    +--- org.apache.logging.log4j:log4j-api:2.22.1
     |    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    \--- org.apache.logging.log4j:log4j-core:2.22.1
     |         \--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.apache.logging.log4j:log4j-api:2.22.1
     +--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
     +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
     |    +--- javax.xml.stream:stax-api:1.0-2
     |    \--- org.codehaus.woodstox:stax2-api:3.0.2
     |         \--- javax.xml.stream:stax-api:1.0-2
     +--- org.eclipse.jetty:jetty-client:11.0.24
     |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
     |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
     |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-http:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
     +--- org.eclipse.jetty:jetty-servlet:11.0.24
     |    +--- org.eclipse.jetty:jetty-security:11.0.24
     |    |    +--- org.eclipse.jetty:jetty-server:11.0.24
     |    |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
     |    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
     |    |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
     |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
     \--- xpp3:xpp3:1.1.4c

testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
No dependencies

(*) - dependencies omitted (listed previously)

(n) - Not resolved (configuration is not meant to be resolved)

A web-based, searchable dependency report is available by adding the --scan option.

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.3.3/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 12s
1 actionable task: 1 executed

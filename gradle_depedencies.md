To honour the JVM settings for this build a single-use Daemon process will be forked. See https://docs.gradle.org/7.3.3/userguide/gradle_daemon.html#sec:disabling_the_daemon.
Daemon will be stopped at the end of the build 

> Task :core:silkwave-api:dependencies

------------------------------------------------------------
Project ':core:silkwave-api'
------------------------------------------------------------

annotationProcessor - Annotation processors and their dependencies for source set 'main'.
No dependencies

api - API dependencies for source set 'main'. (n)
+--- project silkwave-intf (n)
+--- project core-common (n)
+--- com.google.code.gson:gson:2.10.1 (n)
+--- com.google.guava:guava:31.1-jre (n)
+--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.2 (n)
+--- org.apache.activemq:activemq-client:5.18.3 (n)
+--- org.slf4j:slf4j-api:2.0.9 (n)
+--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1 (n)
+--- org.apache.logging.log4j:log4j-api:2.22.1 (n)
+--- org.apache.logging.log4j:log4j-core:2.22.1 (n)
+--- org.codehaus.woodstox:woodstox-core-asl:4.0.8 (n)
+--- org.eclipse.jetty:jetty-client:9.4.50.v20221201 (n)
+--- org.eclipse.jetty:jetty-server:9.4.50.v20221201 (n)
\--- org.eclipse.jetty:jetty-servlet:9.4.50.v20221201 (n)

apiElements - API elements for main. (n)
No dependencies

archives - Configuration for archive artifacts. (n)
No dependencies

compileClasspath - Compile classpath for source set 'main'.
+--- project :core:silkwave-intf
|    \--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
|         \--- jakarta.activation:jakarta.activation-api:2.1.3
+--- project :core:core-common
|    +--- project :core:silkwave-intf (*)
|    +--- com.google.code.gson:gson:2.10.1
|    +--- com.google.guava:guava:31.1-jre
|    |    +--- com.google.guava:failureaccess:1.0.1
|    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    +--- org.checkerframework:checker-qual:3.12.0
|    |    +--- com.google.errorprone:error_prone_annotations:2.11.0
|    |    \--- com.google.j2objc:j2objc-annotations:1.3
|    +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
|    |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
|    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    |         +--- jakarta.activation:jakarta.activation-api:2.1.3
|    |         +--- org.glassfish.jaxb:txw2:4.0.5
|    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
|    +--- jakarta.activation:jakarta.activation-api:2.1.3
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
|    +--- org.apache.activemq:activemq-client:6.1.3
|    |    +--- org.slf4j:slf4j-api:2.0.13
|    |    +--- jakarta.jms:jakarta.jms-api:3.1.0
|    |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
|    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
|    |    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.apache.logging.log4j:log4j-core:2.22.1
|    |    \--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
|    |    +--- javax.xml.stream:stax-api:1.0-2
|    |    \--- org.codehaus.woodstox:stax2-api:3.0.2
|    |         \--- javax.xml.stream:stax-api:1.0-2
|    +--- org.eclipse.jetty:jetty-client:11.0.24
|    |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
|    |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
|    |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-http:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    \--- org.eclipse.jetty:jetty-servlet:11.0.24
|         +--- org.eclipse.jetty:jetty-security:11.0.24
|         |    +--- org.eclipse.jetty:jetty-server:11.0.24
|         |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
|         |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|         |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
|         |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|         |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|         \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
+--- com.google.code.gson:gson:2.10.1
+--- com.google.guava:guava:31.1-jre (*)
+--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.2 -> 4.0.2 (*)
+--- org.apache.activemq:activemq-client:5.18.3 -> 6.1.3 (*)
+--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
+--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1 (*)
+--- org.apache.logging.log4j:log4j-api:2.22.1
+--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
+--- org.codehaus.woodstox:woodstox-core-asl:4.0.8 (*)
+--- org.eclipse.jetty:jetty-client:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.eclipse.jetty:jetty-server:9.4.50.v20221201 -> 11.0.24 (*)
\--- org.eclipse.jetty:jetty-servlet:9.4.50.v20221201 -> 11.0.24 (*)

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
+--- project :core:silkwave-intf
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
|    |    \--- jakarta.activation:jakarta.activation-api:2.1.3
|    \--- jakarta.activation:jakarta.activation-api:2.1.3
+--- project :core:core-common
|    +--- project :core:silkwave-intf (*)
|    +--- com.google.code.gson:gson:2.10.1
|    +--- com.google.guava:guava:31.1-jre
|    |    +--- com.google.guava:failureaccess:1.0.1
|    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    +--- org.checkerframework:checker-qual:3.12.0
|    |    +--- com.google.errorprone:error_prone_annotations:2.11.0
|    |    \--- com.google.j2objc:j2objc-annotations:1.3
|    +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
|    |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
|    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    |         +--- jakarta.activation:jakarta.activation-api:2.1.3
|    |         +--- org.eclipse.angus:angus-activation:2.0.2
|    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.3
|    |         +--- org.glassfish.jaxb:txw2:4.0.5
|    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
|    +--- jakarta.activation:jakarta.activation-api:2.1.3
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
|    +--- org.apache.activemq:activemq-client:6.1.3
|    |    +--- org.slf4j:slf4j-api:2.0.13
|    |    +--- jakarta.jms:jakarta.jms-api:3.1.0
|    |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
|    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
|    |    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    |    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    \--- org.apache.logging.log4j:log4j-core:2.22.1
|    |         \--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
|    +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
|    |    +--- javax.xml.stream:stax-api:1.0-2
|    |    \--- org.codehaus.woodstox:stax2-api:3.0.2
|    |         \--- javax.xml.stream:stax-api:1.0-2
|    +--- org.eclipse.jetty:jetty-client:11.0.24
|    |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
|    |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
|    |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-http:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    +--- org.eclipse.jetty:jetty-servlet:11.0.24
|    |    +--- org.eclipse.jetty:jetty-security:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-server:11.0.24
|    |    |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
|    |    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
|    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    \--- xpp3:xpp3:1.1.4c
+--- com.google.code.gson:gson:2.10.1
+--- com.google.guava:guava:31.1-jre (*)
+--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.2 -> 4.0.2 (*)
+--- org.apache.activemq:activemq-client:5.18.3 -> 6.1.3 (*)
+--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
+--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1 (*)
+--- org.apache.logging.log4j:log4j-api:2.22.1
+--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
+--- org.codehaus.woodstox:woodstox-core-asl:4.0.8 (*)
+--- org.eclipse.jetty:jetty-client:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.eclipse.jetty:jetty-server:9.4.50.v20221201 -> 11.0.24 (*)
\--- org.eclipse.jetty:jetty-servlet:9.4.50.v20221201 -> 11.0.24 (*)

runtimeElements - Elements of runtime for main. (n)
No dependencies

runtimeOnly - Runtime only dependencies for source set 'main'. (n)
No dependencies

testAnnotationProcessor - Annotation processors and their dependencies for source set 'test'.
No dependencies

testCompileClasspath - Compile classpath for source set 'test'.
+--- project :core:silkwave-intf
|    \--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
|         \--- jakarta.activation:jakarta.activation-api:2.1.3
+--- project :core:core-common
|    +--- project :core:silkwave-intf (*)
|    +--- com.google.code.gson:gson:2.10.1
|    +--- com.google.guava:guava:31.1-jre
|    |    +--- com.google.guava:failureaccess:1.0.1
|    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    +--- org.checkerframework:checker-qual:3.12.0
|    |    +--- com.google.errorprone:error_prone_annotations:2.11.0
|    |    \--- com.google.j2objc:j2objc-annotations:1.3
|    +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
|    |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
|    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    |         +--- jakarta.activation:jakarta.activation-api:2.1.3
|    |         +--- org.glassfish.jaxb:txw2:4.0.5
|    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
|    +--- jakarta.activation:jakarta.activation-api:2.1.3
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
|    +--- org.apache.activemq:activemq-client:6.1.3
|    |    +--- org.slf4j:slf4j-api:2.0.13
|    |    +--- jakarta.jms:jakarta.jms-api:3.1.0
|    |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
|    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
|    |    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.apache.logging.log4j:log4j-core:2.22.1
|    |    \--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
|    |    +--- javax.xml.stream:stax-api:1.0-2
|    |    \--- org.codehaus.woodstox:stax2-api:3.0.2
|    |         \--- javax.xml.stream:stax-api:1.0-2
|    +--- org.eclipse.jetty:jetty-client:11.0.24
|    |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
|    |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
|    |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-http:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    \--- org.eclipse.jetty:jetty-servlet:11.0.24
|         +--- org.eclipse.jetty:jetty-security:11.0.24
|         |    +--- org.eclipse.jetty:jetty-server:11.0.24
|         |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
|         |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|         |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
|         |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|         |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|         \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
+--- com.google.code.gson:gson:2.10.1
+--- com.google.guava:guava:31.1-jre (*)
+--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.2 -> 4.0.2 (*)
+--- org.apache.activemq:activemq-client:5.18.3 -> 6.1.3 (*)
+--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
+--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1 (*)
+--- org.apache.logging.log4j:log4j-api:2.22.1
+--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
+--- org.codehaus.woodstox:woodstox-core-asl:4.0.8 (*)
+--- org.eclipse.jetty:jetty-client:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.eclipse.jetty:jetty-server:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.eclipse.jetty:jetty-servlet:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.junit.jupiter:junit-jupiter:5.8.2
|    +--- org.junit:junit-bom:5.8.2
|    |    +--- org.junit.jupiter:junit-jupiter:5.8.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.8.2 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.8.2 (c)
|    |    \--- org.junit.platform:junit-platform-commons:1.8.2 (c)
|    +--- org.junit.jupiter:junit-jupiter-api:5.8.2
|    |    +--- org.junit:junit-bom:5.8.2 (*)
|    |    +--- org.opentest4j:opentest4j:1.2.0
|    |    +--- org.junit.platform:junit-platform-commons:1.8.2
|    |    |    +--- org.junit:junit-bom:5.8.2 (*)
|    |    |    \--- org.apiguardian:apiguardian-api:1.1.2
|    |    \--- org.apiguardian:apiguardian-api:1.1.2
|    \--- org.junit.jupiter:junit-jupiter-params:5.8.2
|         +--- org.junit:junit-bom:5.8.2 (*)
|         +--- org.junit.jupiter:junit-jupiter-api:5.8.2 (*)
|         \--- org.apiguardian:apiguardian-api:1.1.2
+--- org.mockito:mockito-core:5.17.0
|    +--- net.bytebuddy:byte-buddy:1.15.11
|    \--- net.bytebuddy:byte-buddy-agent:1.15.11
\--- org.mockito:mockito-junit-jupiter:5.17.0
     \--- org.mockito:mockito-core:5.17.0 (*)

testCompileOnly - Compile only dependencies for source set 'test'. (n)
No dependencies

testImplementation - Implementation only dependencies for source set 'test'. (n)
+--- org.junit.jupiter:junit-jupiter:5.8.2 (n)
+--- org.mockito:mockito-core:5.17.0 (n)
\--- org.mockito:mockito-junit-jupiter:5.17.0 (n)

testRuntimeClasspath - Runtime classpath of source set 'test'.
+--- project :core:silkwave-intf
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2
|    |    \--- jakarta.activation:jakarta.activation-api:2.1.3
|    \--- jakarta.activation:jakarta.activation-api:2.1.3
+--- project :core:core-common
|    +--- project :core:silkwave-intf (*)
|    +--- com.google.code.gson:gson:2.10.1
|    +--- com.google.guava:guava:31.1-jre
|    |    +--- com.google.guava:failureaccess:1.0.1
|    |    +--- com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava
|    |    +--- com.google.code.findbugs:jsr305:3.0.2
|    |    +--- org.checkerframework:checker-qual:3.12.0
|    |    +--- com.google.errorprone:error_prone_annotations:2.11.0
|    |    \--- com.google.j2objc:j2objc-annotations:1.3
|    +--- org.glassfish.jaxb:jaxb-runtime:4.0.5
|    |    \--- org.glassfish.jaxb:jaxb-core:4.0.5
|    |         +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    |         +--- jakarta.activation:jakarta.activation-api:2.1.3
|    |         +--- org.eclipse.angus:angus-activation:2.0.2
|    |         |    \--- jakarta.activation:jakarta.activation-api:2.1.3
|    |         +--- org.glassfish.jaxb:txw2:4.0.5
|    |         \--- com.sun.istack:istack-commons-runtime:4.1.2
|    +--- jakarta.activation:jakarta.activation-api:2.1.3
|    +--- jakarta.xml.bind:jakarta.xml.bind-api:4.0.2 (*)
|    +--- jakarta.jms:jakarta.jms-api:2.0.3 -> 3.1.0
|    +--- org.apache.activemq:activemq-client:6.1.3
|    |    +--- org.slf4j:slf4j-api:2.0.13
|    |    +--- jakarta.jms:jakarta.jms-api:3.1.0
|    |    \--- org.fusesource.hawtbuf:hawtbuf:1.11
|    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1
|    |    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    |    +--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    \--- org.apache.logging.log4j:log4j-core:2.22.1
|    |         \--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.apache.logging.log4j:log4j-api:2.22.1
|    +--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
|    +--- org.codehaus.woodstox:woodstox-core-asl:4.0.8
|    |    +--- javax.xml.stream:stax-api:1.0-2
|    |    \--- org.codehaus.woodstox:stax2-api:3.0.2
|    |         \--- javax.xml.stream:stax-api:1.0-2
|    +--- org.eclipse.jetty:jetty-client:11.0.24
|    |    +--- org.eclipse.jetty:jetty-alpn-client:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24
|    |    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24
|    |    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-http:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    |    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    +--- org.eclipse.jetty:jetty-util:11.0.24 (*)
|    +--- org.eclipse.jetty:jetty-servlet:11.0.24
|    |    +--- org.eclipse.jetty:jetty-security:11.0.24
|    |    |    +--- org.eclipse.jetty:jetty-server:11.0.24
|    |    |    |    +--- org.eclipse.jetty:jetty-http:11.0.24 (*)
|    |    |    |    +--- org.eclipse.jetty:jetty-io:11.0.24 (*)
|    |    |    |    +--- org.eclipse.jetty.toolchain:jetty-jakarta-servlet-api:5.0.2
|    |    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    |    \--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
|    \--- xpp3:xpp3:1.1.4c
+--- com.google.code.gson:gson:2.10.1
+--- com.google.guava:guava:31.1-jre (*)
+--- jakarta.xml.bind:jakarta.xml.bind-api:2.3.2 -> 4.0.2 (*)
+--- org.apache.activemq:activemq-client:5.18.3 -> 6.1.3 (*)
+--- org.slf4j:slf4j-api:2.0.9 -> 2.0.13
+--- org.apache.logging.log4j:log4j-slf4j2-impl:2.22.1 (*)
+--- org.apache.logging.log4j:log4j-api:2.22.1
+--- org.apache.logging.log4j:log4j-core:2.22.1 (*)
+--- org.codehaus.woodstox:woodstox-core-asl:4.0.8 (*)
+--- org.eclipse.jetty:jetty-client:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.eclipse.jetty:jetty-server:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.eclipse.jetty:jetty-servlet:9.4.50.v20221201 -> 11.0.24 (*)
+--- org.junit.jupiter:junit-jupiter:5.8.2 -> 5.11.4
|    +--- org.junit:junit-bom:5.11.4
|    |    +--- org.junit.jupiter:junit-jupiter:5.11.4 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-api:5.11.4 (c)
|    |    +--- org.junit.platform:junit-platform-commons:1.11.4 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-params:5.11.4 (c)
|    |    +--- org.junit.jupiter:junit-jupiter-engine:5.11.4 (c)
|    |    \--- org.junit.platform:junit-platform-engine:1.11.4 (c)
|    +--- org.junit.jupiter:junit-jupiter-api:5.11.4
|    |    +--- org.junit:junit-bom:5.11.4 (*)
|    |    +--- org.opentest4j:opentest4j:1.3.0
|    |    \--- org.junit.platform:junit-platform-commons:1.11.4
|    |         \--- org.junit:junit-bom:5.11.4 (*)
|    +--- org.junit.jupiter:junit-jupiter-params:5.11.4
|    |    +--- org.junit:junit-bom:5.11.4 (*)
|    |    \--- org.junit.jupiter:junit-jupiter-api:5.11.4 (*)
|    \--- org.junit.jupiter:junit-jupiter-engine:5.11.4
|         +--- org.junit:junit-bom:5.11.4 (*)
|         +--- org.junit.platform:junit-platform-engine:1.11.4
|         |    +--- org.junit:junit-bom:5.11.4 (*)
|         |    +--- org.opentest4j:opentest4j:1.3.0
|         |    \--- org.junit.platform:junit-platform-commons:1.11.4 (*)
|         \--- org.junit.jupiter:junit-jupiter-api:5.11.4 (*)
+--- org.mockito:mockito-core:5.17.0
|    +--- net.bytebuddy:byte-buddy:1.15.11
|    +--- net.bytebuddy:byte-buddy-agent:1.15.11
|    \--- org.objenesis:objenesis:3.3
\--- org.mockito:mockito-junit-jupiter:5.17.0
     +--- org.mockito:mockito-core:5.17.0 (*)
     \--- org.junit.jupiter:junit-jupiter-api:5.11.4 (*)

testRuntimeOnly - Runtime only dependencies for source set 'test'. (n)
No dependencies

(c) - dependency constraint
(*) - dependencies omitted (listed previously)

(n) - Not resolved (configuration is not meant to be resolved)

A web-based, searchable dependency report is available by adding the --scan option.

Deprecated Gradle features were used in this build, making it incompatible with Gradle 8.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

See https://docs.gradle.org/7.3.3/userguide/command_line_interface.html#sec:command_line_warnings

BUILD SUCCESSFUL in 7s
1 actionable task: 1 executed

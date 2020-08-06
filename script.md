# Before we start

* Make sure that the docker daemon is up and running
* Make sure that no image named `scribe` or `scribe-custom` exist in the local registry (run `docker images scribe` and `docker images scribe-custom` to confirm)
* This script assumes that this project uses Java 1.8. Make sure that the project is built.


# Basic Docker image
There's a number of tutorials and blog posts about embedding a Spring Boot app in a container.
Our initial `Dockerfile` is very simple and not optimized:

```dockerfile
FROM openjdk:8-jre-alpine
WORKDIR application
ARG JAR_FILE=scribe/target/scribe-*.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java","-jar","application.jar"]
```

If we haven't build this image yet, let's build it:

```shell script
docker build -t scribe-custom:0.0.1-SNAPSHOT .
```

We can run it like any other Docker image:

```
docker run --rm -p 8080:8080 scribe-custom:0.0.1-SNAPSHOT
```


Use [dive](https://github.com/wagoodman/dive) we can look at the structure of the generated image.

```
dive scribe-custom:0.0.1-SNAPSHOT
```

You can create the following config file at `~/.config/dive/dive.yaml` to hide unmodified files by default:

```yaml
diff:
 hide:
   - unmodified
```


It's not ideal as we have a single layer for the app so everytime something changes, that whole layer has to be created again.

We could improve things a bit by extracting the fat jar and creating separate layers for the application and the dependencies, something like:

```dockerfile
FROM openjdk:8-jdk-alpine AS builder
WORKDIR application
ARG JAR_FILE=scribe/target/scribe-*.jar
COPY ${JAR_FILE} application.jar
RUN jar -xf ./application.jar

FROM openjdk:8-jre-alpine
COPY --from=builder application/BOOT-INF/lib /app/lib
COPY --from=builder application/META-INF /app/META-INF
COPY --from=builder application/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","io.spring.sample.scribe.ScribeApplication"]
```                               

It is better, but the libraries are still on a single layer.

# Application layers 
Spring Boot 2.3 provides a way to split the application in more fine-grained layers.

```
<configuration>
  <layers>
    <enabled>true</enabled>
  </layers>
</configuration>
```

When this mode is enabled, a small utility is included that lets us see the layers and manage them.
For instance, this command lists the available layers:


```
java -Djarmode=layertools -jar scribe/target/scribe-0.0.1-SNAPSHOT.jar list
```

Leads to:

```
dependencies
spring-boot-loader
snapshot-dependencies
application
```

We  can use that tool to extract the layers as part of building the image


```dockerfile
FROM openjdk:8-jdk-alpine AS builder
WORKDIR application
ARG JAR_FILE=scribe/target/scribe-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:8-jre-alpine
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]
```

Let's dive in our image again:

```
dive scribe-custom:0.0.1-SNAPSHOT
```                           

Our application does not use any snapshot but if we did, regular dependencies and snapshot dependencies would have been separated each in their respective layer.
The `spring-boot-loader` that Spring Boot uses to bootstrap an application using `java -jar` is easier to put in the image now it is extracted as a dedicated layer.
As of Spring Boot 2.3, `JarLauncher` can run on an exploded jar.
This is an improvement over specifying the classpath and the fully qualified name of the app.


# Customize layers
Spring Boot 2.3 allows you to define the layers of your application in a more fine-grained manner.
For the sake of the example, let's separate the webjars of our application in a separate layer as they change more often than other dependencies.
We don't use snapshots at the moment, so we can remove that dedicated layer.

Let's first create our custom layers configuration at `src/main/layers.xml`:

```xml
<layers xmlns="http://www.springframework.org/schema/boot/layers"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.springframework.org/schema/boot/layers
                      https://www.springframework.org/schema/boot/layers/layers-2.3.xsd">
    <application>
        <into layer="spring-boot-loader">
            <include>org/springframework/boot/loader/**</include>
        </into>
        <into layer="application"/>
    </application>
    <dependencies>
        <into layer="webjars">
            <include>org.webjars.npm*:*:*</include>
        </into>
        <into layer="dependencies"/>
    </dependencies>
    <layerOrder>
        <layer>dependencies</layer>
        <layer>spring-boot-loader</layer>
        <layer>webjars</layer>
        <layer>application</layer>
    </layerOrder>
</layers>
```              

And update our build configuration to refer to our custom configuration:

```xml
<configuration>
  <layers>
    <enabled>true</enabled>
    <configuration>${project.basedir}/src/main/layers.xml</configuration>    
  </layers>
</configuration>
```

Once we rebuilt the jar, we can list the updated layers as follows:

```shell script
java -Djarmode=layertools -jar scribe/target/scribe-0.0.1-SNAPSHOT.jar list
``` 

This yields:

```
dependencies
spring-boot-loader
webjars
application
```        

We now need to update our `Dockerfile` to include an additional layer.
If we forget to do that, we'll have an exception as the `snapshot-dependencies` layer is gone.
If we `dive` in our container, we can see an additional layer with only our webjars.


# What about jib?

TODO

# Spring Boot 2.3 support
Spring Boot 2.3 includes support for Cloud Native Buildpacks, allowing you to build an image directly from your build.
Let's first remove our custom `Dockerfile` to strengthen that it is no longer in the picture.

From the root directory:

```
./mvnw spring-boot:build-image -pl scribe
```

Once the image is built, we can check it's actually available in our local registry:

```
docker images scribe
```

We can also run the image:

```
docker run --rm -p 8080:8080 scribe:0.0.1-SNAPSHOT
```

By default, the image name is `<artifactId>:<version>`. Let's dive in that generated image:

```
dive scribe:0.0.1-SNAPSHOT
```

We can see that the image has transparently applied our layers configuration that we've defined previously.
Besides that, a number of layers have been contributed based on the nature of the application.
In particular, the buildpack has detected the jar is a Spring Boot application and a number of Spring Boot specific tasks were performed to optimize the environment in which this app runs.


# Tune Java version
Each buildpack exposes a number of properties to enable opt-in behaviour or to tune how the image is 
In the root pom, change `<java.version>` from `1.8` to `11`, we can invoke the `build-image` command again and we can see that JRE 11 is now bundled with the image and that other layers are reused (except the security providers that are based on the JDK).

This happens as the build plugins detect this specific property and auto-configure the buildpack to use the same JRE.

The property to use to customize the Java version is `BP_JVM_VERSION`.

# Going further
There are many other ways to tune how your image is built.
You could create your own builder, reusing a number of existing buildpacks.
You could also deploy your jar file as is and let a separate process create and publish your images (see `kpack).
This lets you manage your image consistently and can be useful if you need to rebase all your applications following a CVE.

FROM openjdk:8-jdk-alpine AS builder
WORKDIR application
ARG JAR_FILE=scribe/target/scribe-*.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM openjdk:8-jre-alpine
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/webjars/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java","org.springframework.boot.loader.JarLauncher"]
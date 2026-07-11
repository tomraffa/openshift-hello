FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest

WORKDIR /opt/app

COPY target/openshift-hello.jar /opt/app/openshift-hello.jar

EXPOSE 8080

ENV PORT=8080

CMD ["java", "-jar", "/opt/app/openshift-hello.jar"]
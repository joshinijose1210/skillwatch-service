FROM gradle:8-jdk17-alpine as builder

RUN mkdir -p /opt/app-root/src && \
    adduser runner -D -h /opt/app-root && \
    chown -R runner:runner /opt/app-root

WORKDIR /opt/app-root/src

ADD --chown=runner build.gradle /opt/app-root/src/build.gradle
ADD --chown=runner dependencies.gradle /opt/app-root/src/dependencies.gradle
ADD --chown=runner settings.gradle /opt/app-root/src/settings.gradle

# Only download dependencies
# Eat the expected build failure since no source code has been copied yet
RUN gradle clean assemble || true

ADD --chown=runner api /opt/app-root/src/api
ADD --chown=runner application /opt/app-root/src/application
ADD --chown=runner connector /opt/app-root/src/connector
ADD --chown=runner migration /opt/app-root/src/migration
ADD --chown=runner core /opt/app-root/src/core

# Do the actual build, minus the test, the test requires docker, while we are running on docker
RUN gradle build -x test

# Use eclipse temurin runner
FROM ibm-semeru-runtimes:open-17-jre as runner

RUN mkdir -p /opt/app-root/src && \
    useradd -m -d /opt/app-root runner && \
    chown -R runner:runner /opt/app-root

WORKDIR /opt/app-root/src

COPY --from=builder --chmod=005 /opt/app-root/src/api/build/libs/sample-service-0.1-all.jar /opt/app-root/src/runner.jar
COPY --from=builder --chmod=005 /opt/app-root/src/migration/build/libs/migration-all.jar /opt/app-root/src/migration.jar
ADD --chmod=005 .entrypoint.sh /opt/app-root/src/entrypoint.sh

USER runner

EXPOSE 9000

ENTRYPOINT ["/opt/app-root/src/entrypoint.sh"]

CMD ["-Dmicronaut.environments=dev", "/opt/app-root/src/runner.jar"]

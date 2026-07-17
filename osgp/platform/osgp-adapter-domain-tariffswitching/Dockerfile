# SPDX-FileCopyrightText: Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0

# WS6: osgp-adapter-domain-tariffswitching now ships as a self-contained Spring Boot fat jar with an
# embedded servlet container instead of a WAR deployed into Tomcat.
FROM eclipse-temurin:17-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /var/log/osp /etc/osgp

WORKDIR /app
COPY target/osgp-adapter-domain-tariffswitching-*.jar app.jar

EXPOSE 8080

# Health probe now uses the Spring Boot Actuator readiness endpoint instead of the hand-made
# probe.txt file that used to be served by Tomcat.
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health/readiness || exit 1

# Honour $JAVA_OPTS (memory/GC settings from the deployment) and open java.base/java.lang for the
# Orika object mapper's runtime bytecode generation (required on JDK 17+).
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS --add-opens java.base/java.lang=ALL-UNNAMED -jar /app/app.jar"]

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace/app

COPY .mvn/ .mvn/
COPY mvnw pom.xml checkstyle.xml sonar-project.properties ./
COPY src/ src/

RUN chmod +x mvnw \
    && ./mvnw -q -ntp -Pprod -DskipTests -Dmodernizer.skip=true package


FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Instala la versión predeterminada de postgresql-client (jammy: 14)
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl postgresql-client \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/app/target/control-expedientes-backend-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

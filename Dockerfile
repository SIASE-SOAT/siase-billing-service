# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests \
    && find /app/target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' -exec cp {} /app/app.jar \;

# ---------- runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 siase && chown -R siase /app
USER siase
COPY --from=build /app/app.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
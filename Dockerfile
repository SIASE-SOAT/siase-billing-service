# ---------- build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- runtime ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 siase && chown -R siase /app
USER siase
COPY --from=build /app/target/siase-billing-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","app.jar"]

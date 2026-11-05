# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# dependencies first - this layer stays cached as long as the pom does not change
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# tests need docker themselves (testcontainers) and run in the pipeline, not in the image build
RUN mvn -B -q package -DskipTests

# ---- runtime ----
FROM eclipse-temurin:21-jre
RUN groupadd --system app && useradd --system --gid app --create-home --home-dir /home/app app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# never run as root inside the container
USER app
EXPOSE 8081

# heap follows the container memory limit instead of a fixed -Xmx
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
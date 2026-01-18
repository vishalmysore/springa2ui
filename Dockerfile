FROM eclipse-temurin:18-jdk AS build

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:18-jre
WORKDIR /app

COPY --from=build /app/target/*.jar springactions-0.2.3.jar

EXPOSE 8080

# IMPORTANT: bind to Render PORT at runtime
CMD ["sh", "-c", "echo PORT=$PORT && java -jar springactions-0.2.3.jar --server.port=${PORT}"]


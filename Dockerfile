FROM eclipse-temurin:17-jdk AS build

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

# Set port using env var
ENV SERVER_PORT=8080

COPY --from=build /app/target/*.jar springactions-0.2.3.jar

EXPOSE 8080

CMD ["java", "-jar", "springactions-0.2.3.jar"]

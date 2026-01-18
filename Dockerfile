FROM eclipse-temurin:18-jdk

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/springactions-0.2.3.jar"]

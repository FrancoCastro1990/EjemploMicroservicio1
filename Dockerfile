FROM eclipse-temurin:21-jdk AS buildstage

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src /app/src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

COPY --from=buildstage /app/target/microservicio-0.0.1-SNAPSHOT.jar /app/app.jar

# Crear directorio para EFS
RUN mkdir -p /app/efs

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
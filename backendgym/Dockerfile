# Etapa de construcción
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiar pom y código fuente
COPY pom.xml .
COPY src ./src

# Compilar el proyecto sin tests
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar el jar generado desde la etapa de build
COPY --from=build /app/target/backendgym-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la app Spring Boot
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","app.jar"]

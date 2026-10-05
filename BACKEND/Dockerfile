# ---- build ---------------------------------------------------------------
# El proyecto declara <java.version>17</java.version>; la imagen anterior
# (maven:3.9.6-eclipse-temurin-25) no existe en el registro.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Capa de dependencias separada para aprovechar la cache entre builds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- runtime -------------------------------------------------------------
# Se usa la variante Ubuntu y no -jre-alpine: Temurin 17 solo publica Alpine
# para x86_64, y esta maquina es arm64.
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
COPY --from=build /app/target/*.jar app.jar
USER spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]

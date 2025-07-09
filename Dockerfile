# Estágio 1: Build com Maven
# Usamos a sintaxe do BuildKit
#syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia os arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# Copia o .jar e o .pom da sua API para dentro do container.
COPY libs/pix-gen-api-1.0.0.jar /app/pix-gen-api.jar
COPY libs/pix-gen-api-1.0.0.pom /app/pix-gen-api.pom

# --- SOLUÇÃO DEFINITIVA ---
# Combina a instalação manual e o build em um único comando RUN.
# Isso garante que a dependência seja instalada no repositório local
# e o build a utilize em seguida, sem tentar acessar a internet para isso.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw install:install-file \
        -Dfile=/app/pix-gen-api.jar \
        -DpomFile=/app/pix-gen-api.pom \
    && ./mvnw -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

# Estágio 1: Build com Maven
# Usamos a sintaxe do BuildKit para habilitar o uso de segredos
#syntax=docker/dockerfile:1

FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia os arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# --- A SOLUÇÃO DEFINITIVA E SEGURA ---
# Usa o Docker Build Secrets para montar o settings.xml.
# O arquivo é disponibilizado apenas para este comando e não é salvo na imagem.
# O 'id' (settings.xml) deve corresponder ao "Filename" do Secret File criado no Render.
# O 'dst' (destination) é o caminho onde o Maven espera encontrar o arquivo de settings.
RUN --mount=type=secret,id=settings.xml,dst=/root/.m2/settings.xml \
    --mount=type=cache,target=/root/.m2/repository \
    ./mvnw -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

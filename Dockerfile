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

# --- SOLUÇÃO: INSTALAÇÃO MANUAL DA DEPENDÊNCIA ---
# 1. Copia o arquivo .jar da sua API (que você vai adicionar ao projeto) para dentro do container.
#    Você precisa criar uma pasta 'libs' no seu projeto e colocar o pix-gen-api-1.0.0.jar dentro dela.
COPY libs/pix-gen-api-1.0.0.jar /app/pix-gen-api.jar

# 2. Usa o Maven para instalar este arquivo .jar no repositório local do container.
#    Quando o Maven for construir o projeto, ele encontrará a dependência aqui e não tentará baixá-la do GitHub.
RUN ./mvnw install:install-file \
    -Dfile=/app/pix-gen-api.jar \
    -DgroupId=com.japaneixxx \
    -DartifactId=pix-gen-api \
    -Dversion=1.0.0 \
    -Dpackaging=jar

# 3. Executa o build normalmente.
#    Agora, o Maven não precisa mais de um settings.xml, pois a dependência já está "offline".
RUN --mount=type=cache,target=/root/.m2/repository \
    ./mvnw -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

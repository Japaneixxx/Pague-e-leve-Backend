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

# --- TESTE DIAGNÓSTICO FINAL (FORÇA BRUTA) ---
# Cria o settings.xml diretamente no container com as credenciais hardcoded.
# ISTO NÃO É SEGURO PARA PRODUÇÃO, É APENAS PARA ISOLAR O PROBLEMA.
# Substitua 'ghp_SEU_TOKEN_AQUI' pelo seu token real e válido.
RUN echo '<settings><servers><server><id>github</id><username>Japaneixxx</username><password>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</password></server></servers></settings>' > /app/settings.xml

# --- VERIFICAÇÃO ---
# Imprime o conteúdo do arquivo para garantir que foi criado corretamente.
RUN cat /app/settings.xml

# Executa o build usando o arquivo que acabamos de criar.
RUN --mount=type=cache,target=/root/.m2/repository \
    ./mvnw -s /app/settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

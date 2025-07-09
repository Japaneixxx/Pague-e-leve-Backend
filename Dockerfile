# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia os arquivos de configuração e build
# Este settings.xml deve usar as variáveis de ambiente
# <username>${env.GITHUB_USERNAME}</username>
# <password>${env.GITHUB_TOKEN}</password>
COPY settings.xml .
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw

# Copia o código fonte da sua aplicação
COPY src ./src

# Executa o build.
# A flag -U força o Maven a verificar por atualizações e ignorar o cache de falhas.
# O Dockerfile assume que as variáveis GITHUB_USERNAME e GITHUB_TOKEN
# serão injetadas pelo ambiente de build do Render.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

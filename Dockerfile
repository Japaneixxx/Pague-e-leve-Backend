# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia os arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# --- PASSO DE VERIFICAÇÃO FINAL ---
# Lista os arquivos no diretório de trabalho atual (/app)
# Se o Secret File foi montado aqui, veremos o "settings.xml" na lista.
RUN ls -la

# Executa o build usando o settings.xml do diretório atual.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia o arquivo de configurações do Maven para dentro do container.
COPY settings.xml .

# PASSO DE DEPURAÇÃO: Lista os arquivos para garantir que settings.xml está aqui.
RUN ls -l

# Copia os arquivos do Maven Wrapper e o pom.xml
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .

# Garante que o script mvnw tem permissões de execução.
RUN chmod +x mvnw

# Baixa as dependências com a flag -X (DEBUG MÁXIMO)
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -X dependency:go-offline

# Copia o resto do código fonte da sua aplicação
COPY src ./src

# Compila e empacota a aplicação, também com a flag -X
RUN ./mvnw -s settings.xml -X clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

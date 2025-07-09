# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Não precisamos mais copiar o settings.xml, pois o Render vai montá-lo como um arquivo secreto.

# Copia os arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# Executa o build usando o "Secret File" montado pelo Render.
# O caminho /etc/secrets/settings.xml é o padrão do Render para arquivos secretos.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s /etc/secrets/settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

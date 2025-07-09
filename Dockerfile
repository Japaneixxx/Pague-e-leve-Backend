# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia o settings.xml do seu projeto para dentro do container.
# Este arquivo DEVE existir na raiz do seu projeto e usar variáveis de ambiente.
COPY settings.xml .

# Copia os arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# Executa o build. O Maven, dentro do container, irá ler as variáveis de ambiente
# GITHUB_USERNAME e GITHUB_TOKEN que foram configuradas no painel do Render.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

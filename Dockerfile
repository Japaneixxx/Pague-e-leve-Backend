# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# NOVO: Copia o arquivo de configurações do Maven para dentro do container.
# Este arquivo contém as credenciais para acessar o GitHub Packages.
COPY settings.xml .

# Copia os arquivos do Maven Wrapper e o pom.xml
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .

# Garante que o script mvnw tem permissões de execução.
RUN chmod +x mvnw

# Baixa as dependências do Maven usando o settings.xml para autenticação.
# A flag "-s settings.xml" é a chave para a solução.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml dependency:go-offline

# Copia o resto do código fonte da sua aplicação
COPY src ./src

# Compila e empacota a aplicação, também usando o settings.xml
RUN ./mvnw -s settings.xml clean install -DskipTests

# Estágio 2: Execução (nenhuma mudança necessária aqui)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Ajuste o nome do JAR se for diferente do gerado pelo seu pom.xml
COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

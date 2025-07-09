# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia o arquivo de configurações do Maven para dentro do container.
COPY settings.xml .

# --- PASSO DE DEPURAÇÃO ---
# Lista os arquivos no diretório de trabalho para garantir que settings.xml está aqui.
RUN ls -l

# Copia os arquivos do Maven Wrapper e o pom.xml
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .

# Garante que o script mvnw tem permissões de execução.
RUN chmod +x mvnw

# Baixa as dependências do Maven com a flag -e para logs de erro detalhados.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -e dependency:go-offline

# Copia o resto do código fonte da sua aplicação
COPY src ./src

# Compila e empacota a aplicação, também com a flag -e.
RUN ./mvnw -s settings.xml -e clean install -DskipTests

# Estágio 2: Execução (nenhuma mudança necessária aqui)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Ajuste o nome do JAR se for diferente do gerado pelo seu pom.xml
COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

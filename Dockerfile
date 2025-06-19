# Estágio de Build: Para construir o JAR da aplicação
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .

# NOVO: Garante que o script mvnw tem permissões de execução.
# Se este comando falhar, o erro será mais específico.
RUN chmod +x mvnw

# NOVO: Adiciona uma verificação para depuração se o mvnw existe
# Remova esta linha após o problema ser resolvido
# RUN ls -l mvnw || echo "mvnw not found in /app"

# Baixa as dependências do Maven (irá usar o cache se nada mudou no pom.xml)
# O comando agora está separado do chmod
RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copia o código fonte da sua aplicação
COPY src ./src

# Compila e empacota a aplicação em um JAR executável
RUN ./mvnw clean install -DskipTests

# Estágio de Execução: Para rodar a aplicação
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
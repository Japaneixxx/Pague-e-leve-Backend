# Estágio de Build: Para construir o JAR da aplicação
# Usa uma imagem oficial do Maven com Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o arquivo pom.xml e os arquivos do wrapper Maven para que o Maven possa baixar as dependências
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
# Esta linha copia o script 'mvnw' (sem o ponto inicial extra no nome)

# A linha 'COPY .mvnw .' FOI REMOVIDA AQUI, pois '.mvnw' não é um arquivo padrão.

# Baixa as dependências do Maven (irá usar o cache se nada mudou no pom.xml)
# Certifique-se de que 'mvnw' tem permissões de execução dentro do contêiner
RUN chmod +x mvnw && --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline

# Copia o código fonte da sua aplicação
COPY src ./src

# Compila e empacota a aplicação em um JAR executável
RUN ./mvnw clean install -DskipTests

# Estágio de Execução: Para rodar a aplicação
# Usa uma imagem oficial do OpenJDK 17 (leve e otimizada para execução)
FROM eclipse-temurin:17-jre-alpine

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR executável do estágio de build
# O nome do JAR deve corresponder ao artifactId e version do seu pom.xml
# Pelo seu pom.xml, o nome é pagueleve-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta 8080 que sua aplicação Spring Boot usa
EXPOSE 8080

# Comando para iniciar a aplicação quando o contêiner for executado
ENTRYPOINT ["java","-jar","app.jar"]
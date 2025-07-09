# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia os arquivos de configuração e build
COPY settings.xml .
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw

# --- PASSO DE VERIFICAÇÃO ---
# Imprime o valor da variável de ambiente para confirmar que o Render a injetou.
# Se esta linha imprimir "Verifying username: ", o problema está na configuração do Render.
# Se imprimir "Verifying username: Japaneixxx", a variável foi lida com sucesso.
RUN echo "Verifying username: $GITHUB_USERNAME"

# Copia o código fonte
COPY src ./src

# Executa o build. A flag -U força a verificação de dependências.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

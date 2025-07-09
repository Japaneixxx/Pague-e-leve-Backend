# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# --- PASSO DE DEPURAÇÃO: Defina suas credenciais aqui APENAS PARA ESTE TESTE ---
# Substitua o valor abaixo pelo seu token real do GitHub
ARG GITHUB_USERNAME=Japaneixxx
ARG GITHUB_TOKEN=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

WORKDIR /app

# Cria o settings.xml diretamente no Dockerfile para o teste
# Isso elimina qualquer erro de cópia ou de leitura de variáveis de ambiente.
RUN echo "<settings><servers><server><id>github</id><username>${GITHUB_USERNAME}</username><password>${GITHUB_TOKEN}</password></server></servers></settings>" > settings.xml

# --- PASSO DE VERIFICAÇÃO ---
# Imprime o conteúdo do settings.xml criado para garantir que está correto
RUN cat settings.xml

# Copia os arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# Executa o build usando o settings.xml que acabamos de criar
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

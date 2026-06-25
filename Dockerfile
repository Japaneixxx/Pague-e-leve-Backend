# =================================================================
# STAGE 1: O "Construtor" (Builder)
# Agora usando explicitamente a versão 21 do JDK
# =================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia os arquivos de configuração do Maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Garante permissão de execução ao Maven Wrapper
RUN chmod +x mvnw

# Baixa as dependências (isso evita baixar tudo a cada build)
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline

# Copia o código-fonte
COPY src ./src

# Compila e empacota a aplicação
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests

# =================================================================
# STAGE 2: A Imagem Final (Runner)
# Também em Java 21 para manter a compatibilidade
# =================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia apenas o .jar gerado no estágio anterior
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
# =================================================================
# STAGE 1: O "Construtor" (Builder)
# Usamos uma imagem oficial que já contém o JDK 17 e o Maven.
# =================================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 1. Copia o pom.xml primeiro.
#    Isso otimiza o cache do Docker: se o pom.xml não mudar,
#    o Docker reutiliza a camada de dependências já baixadas.
COPY pom.xml .

# 2. Baixa todas as dependências do projeto.
#    O Maven agora buscará sua biblioteca diretamente do JitPack.
#    O cache mount acelera builds futuros.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline

# 3. Copia o resto do código-fonte da sua aplicação.
COPY src ./src

# 4. Compila, testa e empacota a aplicação em um .jar executável.
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests

# =================================================================
# STAGE 2: A Imagem Final (Runner)
# Usamos uma imagem base leve, que contém apenas o Java Runtime (JRE).
# =================================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia APENAS o .jar final que foi gerado no estágio 'builder'.
# Isso torna a imagem final pequena e limpa.
COPY --from=builder /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta que a sua aplicação Spring Boot usa (padrão é 8080)
EXPOSE 8080

# Comando para iniciar a sua aplicação quando o container for executado.
ENTRYPOINT ["java", "-jar", "app.jar"]
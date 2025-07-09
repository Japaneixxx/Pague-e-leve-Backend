# Estágio 1: Build com Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copia o settings.xml do seu projeto. Ele vai funcionar como um template.
COPY settings.xml .

# Copia o resto dos arquivos de build e o código fonte
COPY pom.xml .
COPY .mvn .mvn/
COPY mvnw .
RUN chmod +x mvnw
COPY src ./src

# --- A SOLUÇÃO DEFINITIVA ---
# Usa o comando 'sed' para ler o settings.xml como um template,
# substituir os placeholders com os valores das variáveis de ambiente do Render,
# e criar um novo arquivo 'settings-final.xml' com as credenciais corretas.
RUN sed -e "s|\${env.GITHUB_USERNAME}|${GITHUB_USERNAME}|" \
        -e "s|\${env.GITHUB_TOKEN}|${GITHUB_TOKEN}|" \
        settings.xml > settings-final.xml

# --- PASSO DE VERIFICAÇÃO (OPCIONAL) ---
# Imprime o conteúdo do arquivo final para garantir que a substituição funcionou.
# A saída deste comando NÃO DEVE conter as strings "${env.GITHUB_USERNAME}" ou "${env.GITHUB_TOKEN}".
RUN echo "Verificando o arquivo settings-final.xml:" && cat settings-final.xml

# Executa o build usando o arquivo de settings que acabamos de gerar.
RUN --mount=type=cache,target=/root/.m2 ./mvnw -s settings-final.xml -U clean install -DskipTests

# Estágio 2: Execução
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/pagueleve-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

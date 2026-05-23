package com.japaneixxx.pagueleve.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SpaConfig — Configura o Spring Boot para servir o frontend Vite (SPA).
 *
 * Funcionamento:
 * - Arquivos estáticos de /dist são servidos normalmente (CSS, JS, imagens).
 * - Qualquer rota que NÃO seja uma rota de API (/api/**, /{storeId}/api/**)
 *   retorna o index.html para que o roteador client-side funcione.
 *
 * INSTRUÇÃO DE DEPLOY:
 *   1. Execute `npm run build` no projeto frontend.
 *   2. Copie o conteúdo de `dist/` para `src/main/resources/static/`.
 *   3. Remova ou desative as dependências do Thymeleaf no pom.xml (ou apenas
 *      remova os templates em src/main/resources/templates/).
 *   4. Esta configuração cuida do fallback para o index.html.
 */
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Todos os recursos estáticos (gerados pelo Vite)
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // Se o arquivo existe (JS, CSS, imagens, favicon), serve normalmente
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // Para rotas de API e endpoints do backend, não intercepta
                        // (o Spring MVC já cuida disso antes de chegar aqui)

                        // Para tudo mais (rotas do SPA), retorna o index.html
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}

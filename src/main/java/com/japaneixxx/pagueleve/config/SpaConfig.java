package com.japaneixxx.pagueleve.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SpaConfig — Serve o frontend Vite (SPA) pelo Spring Boot.
 *
 * Regra de fallback:
 *   - Se o arquivo existe em /static/ (JS, CSS, imagens) → serve normalmente.
 *   - Se a rota começa com /api/, /lojista/produto/, /produto/, /products/,
 *     /{id}/api/ → NÃO intercepta (o Spring MVC cuida dessas rotas de API).
 *   - Qualquer outra rota → retorna index.html para o router client-side.
 *
 * INSTRUÇÃO DE DEPLOY:
 *   1. npm run build  (na pasta pagueleve-frontend)
 *   2. Copie o conteúdo de dist/ para src/main/resources/static/
 *   3. Este arquivo já está no lugar certo — não precisa de mais nada.
 */
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    // Prefixos que pertencem ao backend — NÃO devem cair no fallback SPA
    private static final String[] API_PREFIXES = {
            "api/",
            "lojista/produto/",
            "produto/",
            "products/",
            "actuator/",
    };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {

                        // 1. Se o arquivo existe (JS, CSS, imagens, favicon) → serve direto
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }

                        // 2. Rotas de API ou backend → retorna null (Spring MVC trata)
                        for (String prefix : API_PREFIXES) {
                            if (resourcePath.startsWith(prefix)) {
                                return null;
                            }
                        }

                        // 3. Qualquer rota SPA desconhecida → index.html
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
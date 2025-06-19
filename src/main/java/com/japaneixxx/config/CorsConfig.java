package com.japaneixxx.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration; // Adicione esta importação

@Configuration // Adicione esta anotação para que o Spring reconheça a configuração
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite CORS para todos os endpoints
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://teste.japaneixxx.xyz", // Seu frontend (seja HTTP ou HTTPS, dependendo do ambiente)
                        "https://teste.japaneixxx.xyz", // Adicione a versão HTTPS do seu frontend
                        "https://back.japaneixxx.xyz", // O próprio domínio do backend (bom para auto-requisições ou verificações)
                        "https://seu-servico.onrender.com" // O domínio padrão do Render para o backend (ajuste para o seu)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite métodos HTTP específicos
                .allowedHeaders("*") // Permite todos os cabeçalhos
                .allowCredentials(true); // Permite credenciais (cookies, autenticação HTTP)
    }

}
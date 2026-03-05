package com.japaneixxx.pagueleve.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class ProxyImageController {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    // Apenas tipos de imagem permitidos
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/avif"
    );

    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
        // Validação básica de URL
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("User-Agent", "Mozilla/5.0 (compatible; PagueLeve/1.0)")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            String contentType = response.headers()
                    .firstValue("Content-Type")
                    .orElse("")
                    .split(";")[0]
                    .trim()
                    .toLowerCase();

            if (!ALLOWED_TYPES.contains(contentType)) {
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
            }

            // Limita a 10MB
            byte[] body = response.body();
            if (body.length > 10 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", contentType);
            headers.set("Cache-Control", "max-age=3600");

            return new ResponseEntity<>(body, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}

package com.japaneixxx.pagueleve.controller;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.service.ProductService;
import com.japaneixxx.util.PixGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * StoreApiController — Endpoints REST adicionais para o frontend client-side.
 *
 * Novos endpoints:
 *   GET  /api/stores/{storeId}                  → dados da loja
 *   GET  /api/stores/{storeId}/products/active  → produtos ativos da loja
 *   POST /{storeId}/api/checkout                → gera brCode PIX (já existia, mantém)
 */
@RestController
public class StoreApiController {

    private static final Logger log = LoggerFactory.getLogger(StoreApiController.class);

    @Autowired
    private ProductService productService;

    // ── Dados da loja ──────────────────────────────────────────────────────────

    @GetMapping("/api/stores/{storeId}")
    public ResponseEntity<?> getStore(@PathVariable Long storeId) {
        return productService.findStoreById(storeId)
                .map(store -> {
                    // Retorna apenas campos públicos (sem senha)
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("id",   store.getId());
                    body.put("name", store.getName());
                    body.put("pix",  store.getPix());
                    return ResponseEntity.ok(body);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Produtos ativos (rota separada para clientes) ───────────────────────────

    @GetMapping("/api/stores/{storeId}/products/active")
    public ResponseEntity<List<Product>> getActiveProducts(@PathVariable Long storeId) {
        List<Product> products = productService.findAllActiveProductsByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    // ── Checkout PIX via JSON (complementa o endpoint Thymeleaf existente) ──────

    @PostMapping("/{storeId}/api/checkout")
    public ResponseEntity<?> checkoutApi(
            @PathVariable Long storeId,
            @RequestBody List<Map<String, Object>> items) {

        Optional<Store> storeOpt = productService.findStoreById(storeId);
        if (storeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Loja não encontrada.");
        }

        Store store = storeOpt.get();
        if (store.getPix() == null || store.getPix().isBlank()) {
            return ResponseEntity.badRequest().body("A loja não está configurada para receber PIX.");
        }

        if (items == null || items.isEmpty()) {
            return ResponseEntity.badRequest().body("Carrinho vazio.");
        }

        // Calcula total verificando preços no servidor (segurança)
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            try {
                Long productId = Long.valueOf(item.get("id").toString());
                int quantity   = Integer.parseInt(item.get("quantity").toString());

                Optional<Product> productOpt = productService.findActiveProductByIdAndStoreId(productId, storeId);
                if (productOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Produto ID " + productId + " não encontrado ou inativo.");
                }
                BigDecimal price = BigDecimal.valueOf(productOpt.get().getPrice());
                total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
            } catch (Exception e) {
                log.warn("Item inválido no checkout: {}", item, e);
                return ResponseEntity.badRequest().body("Item inválido no carrinho.");
            }
        }

        try {
            String brCode = PixGenerator.generatePayload(
                    store.getPix(),
                    total,
                    store.getName(),
                    "BELO HORIZONTE",
                    "***"
            );

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("brCode",      brCode);
            response.put("totalAmount", total);
            response.put("store",       Map.of("id", store.getId(), "name", store.getName()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao gerar PIX para loja {}", storeId, e);
            return ResponseEntity.internalServerError().body("Erro ao gerar o código PIX.");
        }
    }
}

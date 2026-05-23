package com.japaneixxx.pagueleve.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PATCH para ProductController.java
 * ===================================
 * O ProductController original tem métodos que retornam views Thymeleaf.
 * Com o frontend client-side, esses métodos precisam ser SUBSTITUÍDOS
 * por redirecionamentos para o SPA (/#/loja/{id}/...).
 *
 * INSTRUÇÕES:
 * Substitua os métodos listados abaixo no seu ProductController.java existente.
 * Os métodos de API (@ResponseBody) NÃO precisam ser alterados.
 */

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR o método redirectToDefaultStoreRoot():
// ─────────────────────────────────────────────────────────────────────────────

//  ANTES:
//  @RequestMapping("/")
//  public String redirectToDefaultStoreRoot() {
//      return "redirect:/1/";
//  }

//  DEPOIS:
    @RequestMapping("/")
    public String redirectToDefaultStoreRoot() {
        return "redirect:/#/loja/1/";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR showStoreRootPage():
// ─────────────────────────────────────────────────────────────────────────────

//  ANTES: retornava a view "index"
//  DEPOIS: redireciona para o SPA
    @GetMapping("/{storeId}/")
    public String showStoreRootPage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR showStoreHomePage():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/{storeId}/home")
    public String showStoreHomePage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/home";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR showProductDetails():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/produto/{productId}")
    public String showProductDetails(
            @PathVariable Long productId,
            @RequestParam(name = "storeId", required = false) Long storeId) {
        if (storeId != null) {
            return "redirect:/#/loja/" + storeId + "/produto/" + productId;
        }
        return "redirect:/#/loja/1/produto/" + productId;
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR listProductsByStore():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/{storeId}/produtos")
    public String listProductsByStore(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/produtos";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR searchProductsInStore():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/{storeId}/search")
    public String searchProductsInStore(
            @PathVariable Long storeId,
            @RequestParam("name") String name) {
        return "redirect:/#/loja/" + storeId + "/produtos";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR showCartPage():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/{storeId}/cart")
    public String showCartPage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/cart";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR showCheckoutPage():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/{storeId}/checkout")
    public String showCheckoutPage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/checkout";
    }

// ─────────────────────────────────────────────────────────────────────────────
// SUBSTITUIR showEditProductForm():
// ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/lojista/produto/{productId}/editar")
    public String showEditProductForm(@PathVariable Long productId) {
        return "redirect:/#/lojista/editar/" + productId;
    }

// ─────────────────────────────────────────────────────────────────────────────
// REMOVER as dependências do Model e View:
// Apague os imports não utilizados após as substituições:
//   import org.springframework.ui.Model;
//   import org.springframework.stereotype.Controller;  ← trocar por @RestController
//
// IMPORTANTE: O ProductController precisa trocar a anotação da classe:
//   ANTES: @Controller
//   DEPOIS: @RestController
//
// Ou manter @Controller mas remover "implements" e os imports de Model/View.
// A forma mais simples é trocar para @RestController já que todos os métodos
// de API já têm @ResponseBody e os métodos de view viram redirects (String).
// ─────────────────────────────────────────────────────────────────────────────

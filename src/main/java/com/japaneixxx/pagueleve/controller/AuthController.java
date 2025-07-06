// src/main/java/com/japaneixxx/pagueleve/controller/AuthController.java

package com.japaneixxx.pagueleve.controller;

import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap; // NOVO: Import necessário
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String senha = credentials.get("senha");

        Optional<Store> storeOptional = storeRepository.findByLogin(login);

        if (storeOptional.isEmpty()) {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos.");
        }

        Store store = storeOptional.get();

        if (passwordEncoder.matches(senha, store.getSenha())) {
            // --- MUDANÇA AQUI ---
            // Agora retornamos um objeto JSON com uma mensagem e o ID da loja.
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login bem-sucedido!");
            responseBody.put("storeId", store.getId()); // Adiciona o ID da loja na resposta

            return ResponseEntity.ok(responseBody);
        } else {
            return ResponseEntity.status(401).body("Usuário ou senha inválidos.");
        }
    }
}
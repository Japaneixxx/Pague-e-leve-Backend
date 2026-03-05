package com.japaneixxx.pagueleve.controller;

import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Listar todas as lojas ---
    @GetMapping("/stores")
    public ResponseEntity<List<Store>> getAllStores() {
        return ResponseEntity.ok(storeRepository.findAll());
    }

    // --- Criar nova loja ---
    @PostMapping("/stores")
    public ResponseEntity<?> createStore(@RequestBody Map<String, String> body) {
        String name  = body.get("name");
        String pix   = body.get("pix");
        String login = body.get("login");
        String senha = body.get("senha");

        if (name == null || name.isBlank())  return ResponseEntity.badRequest().body("Nome é obrigatório.");
        if (login == null || login.isBlank()) return ResponseEntity.badRequest().body("Login é obrigatório.");
        if (senha == null || senha.isBlank()) return ResponseEntity.badRequest().body("Senha é obrigatória.");

        if (storeRepository.findByLogin(login).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Já existe uma loja com esse login.");
        }

        Store store = new Store(name, pix, login, passwordEncoder.encode(senha));
        Store saved = storeRepository.save(store);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // --- Buscar loja por ID ---
    @GetMapping("/stores/{id}")
    public ResponseEntity<?> getStore(@PathVariable Long id) {
        return storeRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Editar loja ---
    @PutMapping("/stores/{id}")
    public ResponseEntity<?> updateStore(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Optional<Store> optional = storeRepository.findById(id);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Store store = optional.get();

        String name  = body.get("name");
        String pix   = body.get("pix");
        String login = body.get("login");
        String senha = body.get("senha");

        if (name != null && !name.isBlank())   store.setName(name);
        if (pix  != null)                       store.setPix(pix);
        if (login != null && !login.isBlank()) {
            // Garante que o novo login não conflita com outra loja
            Optional<Store> conflict = storeRepository.findByLogin(login);
            if (conflict.isPresent() && !conflict.get().getId().equals(id)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Esse login já está em uso por outra loja.");
            }
            store.setLogin(login);
        }
        if (senha != null && !senha.isBlank()) {
            store.setSenha(passwordEncoder.encode(senha));
        }

        return ResponseEntity.ok(storeRepository.save(store));
    }

    // --- Deletar loja ---
    @DeleteMapping("/stores/{id}")
    public ResponseEntity<?> deleteStore(@PathVariable Long id) {
        if (!storeRepository.existsById(id)) return ResponseEntity.notFound().build();
        storeRepository.deleteById(id);
        return ResponseEntity.ok("Loja removida com sucesso.");
    }
}

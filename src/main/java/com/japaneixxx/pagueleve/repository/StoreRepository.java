package com.japaneixxx.pagueleve.repository;

import com.japaneixxx.pagueleve.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // Importar Optional

public interface StoreRepository extends JpaRepository<Store, Long> {
    // Método para procurar uma loja pelo nome (útil no CommandLineRunner)
    Optional<Store> findByName(String name);
}
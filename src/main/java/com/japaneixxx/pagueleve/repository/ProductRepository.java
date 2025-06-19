package com.japaneixxx.pagueleve.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable; // Adicionar import

import com.japaneixxx.pagueleve.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    //Metodo para procurar produtos por ID da loja (Spring Data JPA mapeia para store.id)
    List<Product> findByStoreId(Long storeId);

    //Metodo para procurar um produto pelo ID e ID da loja (Spring Data JPA mapeia para store.id)
    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    // Procura produtos que são destaques
    List<Product> findByHighlightedTrue();

    // Procura produtos de uma loja que são destaques (Spring Data JPA mapeia para store.id)
    List<Product> findByStoreIdAndHighlightedTrue(Long storeId);

    // Procura produtos cujo nome contenha o termo de busca (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Procura produtos cujo nome contenha o termo de busca (case-insensitive) dentro de uma loja específica
    List<Product> findByStoreIdAndNameContainingIgnoreCase(Long storeId, String name);

    // Procura produtos cujo nome contenha o termo de busca (case-insensitive), e é limitado por um número
    List<Product> findByNameContainingIgnoreCaseOrderByNameAsc(String name, Pageable pageable);

    // Procura produtos cujo nome contenha o termo de busca (case-insensitive) dentro de uma loja específica, ordenado e com limite
    List<Product> findByStoreIdAndNameContainingIgnoreCaseOrderByNameAsc(Long storeId, String name, Pageable pageable);
}
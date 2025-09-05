package com.japaneixxx.pagueleve.repository;

import com.japaneixxx.pagueleve.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.store.id = :storeId AND p.active = true")
    List<Product> findByStoreIdAndActiveTrue(@Param("storeId") Long storeId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.store.id = :storeId")
    List<Product> findByStoreId(@Param("storeId") Long storeId);

    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    Optional<Product> findByIdAndStoreIdAndActiveTrue(Long id, Long storeId);

    List<Product> findByHighlightedTrueAndActiveTrue();

    List<Product> findByStoreIdAndHighlightedTrueAndActiveTrue(Long storeId);

    List<Product> findByStoreIdAndNameContainingIgnoreCaseAndActiveTrue(Long storeId, String name);

    Page<Product> findByStoreIdAndNameContainingIgnoreCaseAndActiveTrue(Long storeId, String name, Pageable pageable);

    Optional<Product> findByCodigoDeBarras(String codigoDeBarras);

    // Manter os métodos antigos para não quebrar a área do lojista que precisa ver todos
    List<Product> findByStoreIdAndHighlightedTrue(Long storeId);
    List<Product> findByStoreIdAndNameContainingIgnoreCase(Long storeId, String name);
    Page<Product> findByStoreIdAndNameContainingIgnoreCase(Long storeId, String name, Pageable pageable);
}

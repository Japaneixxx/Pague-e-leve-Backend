package com.japaneixxx.pagueleve.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.japaneixxx.pagueleve.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // This interface extends JpaRepository, which provides basic CRUD operations
    // for the Product entity with Long as the ID type.

    //Metodo para procurar produtos por ID da loja
    List<Product> findByStoreId(Long storeId);

    //Metodo para procurar um produto pelo ID e ID da loja
    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    // NOVO METODO: Procura produtos que são destaques
    List<Product> findByHighlightedTrue();

    // NOVO METODO: Procura produtos de uma loja que são destaques
    List<Product> findByStoreIdAndHighlightedTrue(Long storeId);
}
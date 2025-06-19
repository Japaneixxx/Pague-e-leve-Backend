package com.japaneixxx.pagueleve.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store; // Importar Store
import com.japaneixxx.pagueleve.repository.ProductRepository;
import com.japaneixxx.pagueleve.repository.StoreRepository; // Importar StoreRepository
import org.springframework.transaction.annotation.Transactional;


@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository; // NOVO: Injetar StoreRepository



    @Autowired
    public ProductService(ProductRepository productRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository; // Atribuir StoreRepository
    }

    // Procura todos os produtos

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }


    //Procura um produto pelo id
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    //Procura todos os produtos de uma loja pelo id da loja
    public List<Product> findAllProductsByStoreId(Long storeId) {
        return productRepository.findByStoreId(storeId);
    }

    //Procura um produto pelo id e id da loja
    public Optional<Product> findProductByIdAndStoreId(Long id, Long storeId) {
        return productRepository.findByIdAndStoreId(id, storeId);
    }

    //Salva um produto
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
    //Deleta um produto pelo id
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }
    //Edita um produto
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }
    //Metodo para verificar se o produto existe pelo id e o id da loja
    public boolean productExistsByIdAndStoreId(Long id, Long storeId) {
        return productRepository.findByIdAndStoreId(id, storeId).isPresent();
    }

    // Encontra todos os produtos marcados como destaque
    public List<Product> findHighlightedProducts() {
        return productRepository.findByHighlightedTrue();
    }

    // Encontra todos os produtos marcados como destaque de uma loja específica
    @Transactional(readOnly = true) // Garante que a operação ocorra em uma transação de leitura
    public List<Product> findHighlightedProductsByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndHighlightedTrue(storeId);
    }

    // Buscar produtos por termo de busca, limitado a um número de resultados.
    public List<Product> searchProductsByName(String searchTerm, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByNameContainingIgnoreCaseOrderByNameAsc(searchTerm, pageable);
    }

    // Buscar produtos por termo de busca e ID da loja (sem limite)
    public List<Product> searchProductsByNameAndStoreId(String searchTerm, Long storeId) {
        return productRepository.findByStoreIdAndNameContainingIgnoreCase(storeId, searchTerm);
    }

    // NOVO MÉTODO: Encontrar uma loja pelo ID
    public Optional<Store> findStoreById(Long storeId) {
        return storeRepository.findById(storeId);
    }

    // NOVO MÉTODO: Buscar produtos por termo de busca, ID da loja E limitado a um número de resultados.
    public List<Product> searchProductsByNameAndStoreIdWithLimit(String searchTerm, Long storeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findByStoreIdAndNameContainingIgnoreCaseOrderByNameAsc(storeId, searchTerm, pageable);
    }
}
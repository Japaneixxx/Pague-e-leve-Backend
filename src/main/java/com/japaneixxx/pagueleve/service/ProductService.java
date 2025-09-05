package com.japaneixxx.pagueleve.service;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.repository.ProductRepository;
import com.japaneixxx.pagueleve.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, StoreRepository storeRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    // --- MÉTODOS PARA CLIENTES (APENAS PRODUTOS ATIVOS) ---

    @Transactional(readOnly = true)
    public List<Product> findAllActiveProductsByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndActiveTrue(storeId);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findActiveProductByIdAndStoreId(Long id, Long storeId) {
        return productRepository.findByIdAndStoreIdAndActiveTrue(id, storeId);
    }

    @Transactional(readOnly = true)
    public List<Product> findActiveHighlightedProductsByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndHighlightedTrueAndActiveTrue(storeId);
    }

    @Transactional(readOnly = true)
    public List<Product> searchActiveProductsByNameAndStoreId(String searchTerm, Long storeId) {
        return productRepository.findByStoreIdAndNameContainingIgnoreCaseAndActiveTrue(storeId, searchTerm);
    }

    @Transactional(readOnly = true)
    public List<Product> searchActiveProductsByNameAndStoreIdWithLimit(String searchTerm, Long storeId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("name").ascending());
        return productRepository.findByStoreIdAndNameContainingIgnoreCaseAndActiveTrue(storeId, searchTerm, pageable).getContent();
    }


    // --- MÉTODOS PARA LOJISTAS (TODOS OS PRODUTOS) ---

    @Transactional(readOnly = true)
    public List<Product> findAllProductsByStoreIdForLojista(Long storeId) {
        return productRepository.findByStoreId(storeId);
    }

    @Transactional(readOnly = true)
    public Optional<Product> findProductByIdAndStoreIdForLojista(Long id, Long storeId) {
        return productRepository.findByIdAndStoreId(id, storeId);
    }


    // --- MÉTODOS GERAIS ---

    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    public Optional<Store> findStoreById(Long storeId) {
        return storeRepository.findById(storeId);
    }

    @Transactional
    public Optional<Product> updateProductStatus(Long productId, boolean active) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            product.setActive(active);
            productRepository.save(product);
        }
        return productOptional;
    }

    public Optional<Product> findByCodigoDeBarras(String codigoDeBarras) {
        return productRepository.findByCodigoDeBarras(codigoDeBarras);
    }
}

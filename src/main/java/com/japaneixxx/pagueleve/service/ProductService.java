package com.japaneixxx.pagueleve.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // Importe esta anotação.

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Método para buscar todos os produtos.
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // Método para buscar um produto pelo seu ID.
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    // Método para buscar todos os produtos de uma loja específica.
    public List<Product> findAllProductsByStoreId(Long storeId) {
        return productRepository.findByStoreId(storeId);
    }

    // Método para buscar um produto pelo seu ID e pelo ID da loja.
    public Optional<Product> findProductByIdAndStoreId(Long id, Long storeId) {
        return productRepository.findByIdAndStoreId(id, storeId);
    }

    // Método para salvar um novo produto ou atualizar um existente.
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Método para deletar um produto pelo seu ID.
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    // Método para atualizar um produto existente (chama save, que funciona como upsert).
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    // Método para verificar se um produto existe pelo ID e ID da loja.
    public boolean productExistsByIdAndStoreId(Long id, Long storeId) {
        return productRepository.findByIdAndStoreId(id, storeId).isPresent();
    }

    // NOVO MÉTODO: Encontra todos os produtos marcados como destaque
    public List<Product> findHighlightedProducts() {
        return productRepository.findByHighlightedTrue();
    }

    // NOVO MÉTODO: Encontra todos os produtos marcados como destaque de uma loja específica
    public List<Product> findHighlightedProductsByStoreId(Long storeId) {
        return productRepository.findByStoreIdAndHighlightedTrue(storeId);
    }
}
package com.japaneixxx.pagueleve.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.repository.ProductRepository;

public class ProductService {
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //Procura todos os produtos
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
        return productRepository.findByIdAndStoreID(id, storeId);
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
        return productRepository.findByIdAndStoreID(id, storeId).isPresent();
    }
}

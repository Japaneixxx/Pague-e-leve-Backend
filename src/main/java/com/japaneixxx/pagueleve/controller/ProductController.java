package com.japaneixxx.pagueleve.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.service.ProductService;

public class ProductController {

    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //Endpoint para exibir a página de detalhes de um produto
    //Ex: /produto/1?storeId=10 (para produto com ID 1 na loja com ID 10)
    @GetMapping("/produto/{productId}")
    public String showProductDetails(@PathVariable Long productId, 
                                    @RequestParam(name = "storeId", required = false) Long storeId,
                                    Model model) {
        Optional<Product> productOptional;
        if (storeId != null) {
            productOptional = productService.findProductByIdAndStoreId(productId, storeId);
        } else {
            productOptional = productService.findProductById(productId);
            System.out.println("Atenção: storeId não foi fornecido, usando apenas o ID do produto.");
        }

        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get()); // Adiciona o produto ao modelo ao Thymeleaf
            return "produtoTemplate"; // Retorna o nome do template HTML (produtoTemplate.html)
        } else {
            //Product não encontrado, exibe uma mensagem de erro
            model.addAttribute("error", "Produto não encontrado.");
            return "errorTemplate"; // Retorna um template de erro se o produto não for encontrado
        }
    }

    //Endpoint para listar todos os produtos de uma loja (opcional)
    //Ex: /loja/10/produtos
    @GetMapping("/loja/{storeId}/produtos")
    public String listProductsByStore(@PathVariable Long storeId, Model model) {
        List<Product> products = productService.findAllProductsByStoreId(storeId);
        model.addAttribute("products", products); // Adiciona a lista de produtos ao modelo
        return "store-products-list"; // Retorna o nome do template HTML (store-products-list.html)
    }

    //Endpoint para a home.html (se voce quiser que ela exiba produtos dinamicamente)
    //Ex: /home
    @GetMapping("/home")
    public String showHomePage(Model model) {
        // Voce pode buscar todos os produtos ou produtos de uma loja aqui
        List<Product> products = productService.findAllProducts(); // ou findAllProductsByStoreId(storeId) se quiser filtrar por loja
        model.addAttribute("products", products); // Adiciona a lista de produtos ao modelo
        return "home"; // Retorna o nome do template HTML (home.html)
    }

}

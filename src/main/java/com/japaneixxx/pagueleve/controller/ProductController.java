package com.japaneixxx.pagueleve.controller;

import java.util.List;
import java.util.Optional;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
// A anotação @RequestMapping("/api") foi removida para acessar as URLs diretamente da raiz.
public class ProductController {

    private final ProductService productService;

    @Autowired // Injeção de dependência do ProductService
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Endpoint para exibir a página de detalhes de um produto específico.
     * Acessível via: /produto/{productId} ou /produto/{productId}?storeId={storeId}
     *
     * @param productId O ID do produto a ser exibido.
     * @param storeId Opcional: O ID da loja à qual o produto pertence, para busca mais específica.
     * @param model O objeto Model para passar dados para a view (Thymeleaf).
     * @return O nome do template Thymeleaf (produtoTemplate.html) ou errorTemplate.html se não encontrado.
     */
    @GetMapping("/produto/{productId}")
    public String showProductDetails(@PathVariable Long productId,
                                     @RequestParam(name = "storeId", required = false) Long storeId,
                                     Model model) {
        Optional<Product> productOptional;
        if (storeId != null) {
            // Busca o produto por ID e ID da loja se storeId for fornecido
            productOptional = productService.findProductByIdAndStoreId(productId, storeId);
            System.out.println("Buscando produto com ID: " + productId + " e StoreId: " + storeId);
        } else {
            // Caso contrário, busca o produto apenas por ID
            productOptional = productService.findProductById(productId);
            System.out.println("Atenção: storeId não foi fornecido, usando apenas o ID do produto para busca: " + productId);
        }

        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get()); // Adiciona o produto ao modelo
            return "produtoTemplate"; // Retorna o template de detalhes do produto
        } else {
            model.addAttribute("error", "Produto não encontrado."); // Adiciona mensagem de erro ao modelo
            System.out.println("Produto com ID: " + productId + (storeId != null ? " e StoreId: " + storeId : "") + " não encontrado.");
            return "errorTemplate"; // Retorna um template de erro (você precisaria criar errorTemplate.html)
        }
    }

    /**
     * Endpoint para listar todos os produtos de uma loja específica.
     * Acessível via: /loja/{storeId}/produtos
     *
     * @param storeId O ID da loja cujos produtos serão listados.
     * @param model O objeto Model para passar dados para a view (Thymeleaf).
     * @return O nome do template Thymeleaf (storeProductsTemplate.html).
     */
    @GetMapping("/loja/{storeId}/produtos")
    public String listProductsByStore(@PathVariable Long storeId, Model model) {
        List<Product> products = productService.findAllProductsByStoreId(storeId);
        model.addAttribute("products", products); // Adiciona a lista de produtos da loja ao modelo
        model.addAttribute("currentStoreId", storeId); // Adiciona o ID da loja atual ao modelo para exibição
        System.out.println("Buscando produtos para a loja com ID: " + storeId + ". Encontrados: " + products.size());
        if (products.isEmpty()) {
            System.out.println("Nenhum produto encontrado para a loja com ID: " + storeId);
        }
        return "storeProductsTemplate"; // Retorna o template de listagem de produtos da loja
    }

    /**
     * Endpoint para a página inicial (Home).
     * Acessível via: /home
     *
     * @param model O objeto Model para passar dados para a view (Thymeleaf).
     * @return O nome do template Thymeleaf (home.html).
     */
    @GetMapping("/home")
    public String showHomePage(Model model) {
        // Busca apenas os produtos marcados como destaque para a página inicial
        List<Product> highlightedProducts = productService.findHighlightedProducts();
        model.addAttribute("highlightedProducts", highlightedProducts); // Adiciona a lista de produtos destacados ao modelo
        System.out.println("Carregando página inicial com " + highlightedProducts.size() + " produtos em destaque.");
        return "home"; // Retorna o template da página inicial
    }
}
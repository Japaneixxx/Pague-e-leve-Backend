package com.japaneixxx.pagueleve.controller;

import java.util.List;
import java.util.Optional;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping; // Importar para RequestMapping

@Controller
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Endpoint para a raiz da aplicação (http://localhost:8080/) que redireciona para a raiz da loja 1
    @RequestMapping("/")
    public String redirectToDefaultStoreRoot() {
        return "redirect:/1/"; // Redireciona para o novo endpoint /{storeId}/
    }

    /**
     * NOVO PRINCIPAL ENDPOINT para a página inicial de cada loja: acessível diretamente via /{storeId}/
     * A página index.html agora será um template Thymeleaf dinâmico.
     *
     * @param storeId O ID da loja.
     * @param model O objeto Model.
     * @return O nome do template index.html.
     */
    @GetMapping("/{storeId}/") // Mapeia para /{storeId}/
    public String showStoreRootPage(@PathVariable Long storeId, Model model) {
        Optional<Store> storeOptional = productService.findStoreById(storeId);

        if (storeOptional.isPresent()) {
            Store store = storeOptional.get();
            model.addAttribute("store", store); // Passa o objeto Store completo
            model.addAttribute("currentStoreId", storeId); // Passa o ID da loja para uso no link
            System.out.println("Carregando página inicial de entrada para Loja ID " + storeId + " (" + store.getName() + ").");
            return "index"; // Retorna o template index.html (agora em templates/)
        } else {
            model.addAttribute("error", "Loja não encontrada.");
            System.out.println("Loja com ID: " + storeId + " não encontrada.");
            return "errorTemplate";
        }
    }


    /**
     * Endpoint para a página inicial (Home) específica de uma loja.
     * Acessível via: /{storeId}/home
     * Mantido para links internos que já usam /home.
     *
     * @param storeId O ID da loja cujos produtos serão exibidos.
     * @param model O objeto Model para passar dados para a view (Thymeleaf).
     * @return O nome do template Thymeleaf (home.html) ou errorTemplate.html se a loja não for encontrada.
     */
    @GetMapping("/{storeId}/home")
    public String showStoreHomePage(@PathVariable Long storeId, Model model) {
        Optional<Store> storeOptional = productService.findStoreById(storeId);

        if (storeOptional.isPresent()) {
            Store store = storeOptional.get();
            List<Product> highlightedProducts = productService.findHighlightedProductsByStoreId(storeId);

            model.addAttribute("store", store);
            model.addAttribute("highlightedProducts", highlightedProducts);
            model.addAttribute("currentStoreId", storeId);

            System.out.println("Carregando página inicial da Loja ID " + storeId + " (" + store.getName() + ") com " + highlightedProducts.size() + " produtos em destaque.");
            return "home";
        } else {
            model.addAttribute("error", "Loja não encontrada.");
            System.out.println("Loja com ID: " + storeId + " não encontrada.");
            return "errorTemplate";
        }
    }

    /**
     * Endpoint para exibir a página de detalhes de um produto específico.
     * Acessível via: /produto/{productId} ou /produto/{productId}?storeId={storeId}
     */
    @GetMapping("/produto/{productId}")
    public String showProductDetails(@PathVariable Long productId,
                                     @RequestParam(name = "storeId", required = false) Long storeId,
                                     Model model) {
        Optional<Product> productOptional;
        if (storeId != null) {
            productOptional = productService.findProductByIdAndStoreId(productId, storeId);
            System.out.println("Buscando produto com ID: " + productId + " e StoreId: " + storeId);
        } else {
            productOptional = productService.findProductById(productId);
            System.out.println("Atenção: storeId não foi fornecido, usando apenas o ID do produto.");
        }

        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get());
            return "produtoTemplate";
        } else {
            model.addAttribute("error", "Produto não encontrado.");
            System.out.println("Produto com ID: " + productId + (storeId != null ? " e StoreId: " + storeId : "") + " não encontrado.");
            return "errorTemplate";
        }
    }

    /**
     * Endpoint para listar todos os produtos de uma loja específica.
     * Acessível via: /loja/{storeId}/produtos
     */
    @GetMapping("/{storeId}/produtos")
    public String listProductsByStore(@PathVariable Long storeId, Model model) {
        List<Product> products = productService.findAllProductsByStoreId(storeId);
        model.addAttribute("products", products);
        model.addAttribute("currentStoreId", storeId);
        System.out.println("Buscando produtos para a loja com ID: " + storeId + ". Encontrados: " + products.size());
        if (products.isEmpty()) {
            System.out.println("Nenhum produto encontrado para a loja com ID: " + storeId);
        }
        return "storeProductsTemplate";
    }

    /**
     * Endpoint REST para buscar produtos por nome DENTRO DE UMA LOJA, com limite.
     * Usado pela busca em tempo real na home.html.
     * Retorna uma lista de produtos em JSON.
     * Acessível via: /products/search?name={searchTerm}&storeId={storeId}
     */
    @GetMapping("/products/search")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam("name") String name,
                                        @RequestParam("storeId") Long storeId) {
        int limit = 3;
        List<Product> foundProducts = productService.searchProductsByNameAndStoreIdWithLimit(name, storeId, limit);
        System.out.println("Busca por '" + name + "' na loja " + storeId + ": Encontrados " + foundProducts.size() + " produtos.");
        return foundProducts;
    }

    /**
     * Endpoint para buscar produtos por nome dentro de uma loja e exibir em uma nova página.
     * Acessível via: /loja/{storeId}/search?name={searchTerm}
     */
    @GetMapping("/{storeId}/search")
    public String searchProductsInStore(@PathVariable Long storeId,
                                        @RequestParam("name") String name,
                                        Model model) {
        List<Product> foundProducts = productService.searchProductsByNameAndStoreId(name, storeId);
        model.addAttribute("products", foundProducts);
        model.addAttribute("currentStoreId", storeId);
        model.addAttribute("searchTerm", name);
        System.out.println("Busca na Loja " + storeId + " por '" + name + "': Encontrados " + foundProducts.size() + " produtos.");
        return "storeProductsTemplate"; // Reutilizando o template existente
    }

    /**
     * Exibe o carrinho de compras para uma loja específica.
     * Acessível via: /{storeId}/cart
     *
     * @param storeId O ID da loja cujo carrinho será exibido.
     * @param model O objeto Model para passar dados para o view (Thymeleaf).
     * @return O nome do template Thymeleaf (cart.html) ou errorTemplate.html se a loja não for encontrada.
     */
    @GetMapping("/{storeId}/cart") // Endpoint padronizado
    public String showCartPage(@PathVariable Long storeId, Model model) {
        Optional<Store> storeOptional = productService.findStoreById(storeId);
        if (storeOptional.isPresent()) {
            model.addAttribute("currentStoreId", storeId);
            model.addAttribute("store", storeOptional.get());
            System.out.println("Carregando carrinho para Loja ID: " + storeId);
            return "cart";
        } else {
            model.addAttribute("error", "Loja não encontrada para exibir o carrinho.");
            System.out.println("Loja com ID: " + storeId + " não encontrada para o carrinho.");
            return "errorTemplate";
        }
    }
}
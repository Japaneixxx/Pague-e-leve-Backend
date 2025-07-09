package com.japaneixxx.pagueleve.controller;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.service.ImageUploadService;
import com.japaneixxx.pagueleve.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.japaneixxx.pagueleve.util.PixGenerator;
import java.math.BigDecimal;



import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    // É uma boa prática usar um logger em vez de System.out.println
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ImageUploadService imageUploadService;

    @Autowired
    public ProductController(ProductService productService, ImageUploadService imageUploadService) {
        this.productService = productService;
        this.imageUploadService = imageUploadService;
    }

    /**
     * Processa a criação de um novo produto.
     * Agora aceita tanto um arquivo (imageFile) quanto uma URL de imagem (imageUrl).
     */
    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "featured", required = false, defaultValue = "false") boolean featured,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "codigoDeBarras", required = false) String codigoDeBarras,// NOVO PARÂMETRO
            @RequestParam("storeId") Long storeId) {

        Optional<Store> storeOptional = productService.findStoreById(storeId);
        if (storeOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Loja com ID " + storeId + " não encontrada. Faça o login novamente.");
        }

        try {
            String finalImageUrl;

            // LÓGICA DE PRIORIDADE PARA A IMAGEM
            if (imageFile != null && !imageFile.isEmpty()) {
                // 1. Se um arquivo foi enviado, ele tem a maior prioridade.
                log.info("Criando produto com imagem via UPLOAD de arquivo.");
                finalImageUrl = imageUploadService.uploadImage(imageFile);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                // 2. Se não houver arquivo, mas houver um link, use o link.
                log.info("Criando produto com imagem via LINK.");
                finalImageUrl = imageUrl;
            } else {
                // 3. Se nenhum dos dois for fornecido, usa a imagem padrão.
                log.info("Criando produto com imagem PADRÃO.");
                finalImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHICWZcFeQ7UuaU7N30-E4Vt1GaTYIU1DIEA&s";
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setHighlighted(featured);
            product.setImageUrl(finalImageUrl); // Salva a URL decidida
            product.setCodigoDeBarras(codigoDeBarras);
            product.setStore(storeOptional.get());

            productService.saveProduct(product);

            return ResponseEntity.ok().body("Produto criado com sucesso!");

        } catch (IOException e) {
            log.error("Erro de IO ao fazer upload da imagem para o produto '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar o arquivo de imagem.");
        } catch (Exception e) {
            log.error("Erro inesperado ao criar o produto '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro inesperado no servidor.");
        }
    }

    /**
     * Exibe o formulário para editar um produto existente.
     */
    @GetMapping("/lojista/produto/{productId}/editar")
    public String showEditProductForm(@PathVariable Long productId, Model model) {
        Optional<Product> productOptional = productService.findProductById(productId);

        if (productOptional.isPresent()) {
            model.addAttribute("product", productOptional.get());
            return "lojistaEditarProduto";
        } else {
            model.addAttribute("error", "Produto com ID " + productId + " não encontrado.");
            return "errorTemplate";
        }
    }

    /**
     * Processa a submissão do formulário de edição de produto.
     * Agora aceita tanto um arquivo (imageFile) quanto uma URL de imagem (imageUrl).
     */
    @PostMapping("/api/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "featured", required = false, defaultValue = "false") boolean featured,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "codigoDeBarras", required = false) String codigoDeBarras, // NOVO PARÂMETRO
            @RequestParam("storeId") Long storeId) {

        Optional<Product> existingProductOptional = productService.findProductByIdAndStoreId(productId, storeId);
        if (existingProductOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado. Você não tem permissão para editar este produto.");
        }

        Product productToUpdate = existingProductOptional.get();

        try {
            // LÓGICA DE PRIORIDADE PARA A IMAGEM
            if (imageFile != null && !imageFile.isEmpty()) {
                // 1. Se um arquivo foi enviado, ele tem a maior prioridade.
                log.info("Atualizando produto {} com imagem via UPLOAD de arquivo.", productId);
                String newImageUrl = imageUploadService.uploadImage(imageFile);
                productToUpdate.setImageUrl(newImageUrl);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                // 2. Se não houver arquivo, mas houver um link, use o link.
                log.info("Atualizando produto {} com imagem via LINK.", productId);
                productToUpdate.setImageUrl(imageUrl);
            }
            // 3. Se nenhum dos dois for fornecido, a imagem existente não é alterada.

            // Atualiza os outros campos do produto
            productToUpdate.setName(name);
            productToUpdate.setDescription(description);
            productToUpdate.setPrice(price);
            productToUpdate.setHighlighted(featured);
            productToUpdate.setCodigoDeBarras(codigoDeBarras);

            productService.saveProduct(productToUpdate);

            return ResponseEntity.ok().body("Produto atualizado com sucesso!");

        } catch (IOException e) {
            log.error("Erro de IO ao fazer upload da nova imagem para o produto {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao processar a nova imagem.");
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar o produto {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro inesperado no servidor.");
        }
    }

    /**
     * API para buscar todos os produtos de uma loja específica.
     */
    @GetMapping("/api/stores/{storeId}/products")
    @ResponseBody
    public ResponseEntity<?> getProductsForStore(@PathVariable Long storeId) {
        List<Product> products = productService.findAllProductsByStoreId(storeId);
        return ResponseEntity.ok(products);
    }

    /**
     * API para excluir um produto.
     */
    @DeleteMapping("/api/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @RequestParam Long storeId) {
        Optional<Product> productOptional = productService.findProductByIdAndStoreId(productId, storeId);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado. Você não tem permissão para excluir este produto.");
        }

        try {
            productService.deleteProductById(productId);
            return ResponseEntity.ok().body("Produto excluído com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao tentar excluir o produto {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Não foi possível excluir o produto.");
        }
    }

    /**
     * Prepara e exibe a página de checkout com os dados do PIX.
     * Acessível via: GET /{storeId}/checkout?total=XX.XX
     * VERSÃO ATUALIZADA USANDO NOSSO PRÓPRIO GERADOR DE PIX.
     */
    @GetMapping("/{storeId}/checkout")
    public String showCheckoutPage(@PathVariable Long storeId,
                                   @RequestParam("total") BigDecimal total,
                                   Model model) {

        Optional<Store> storeOptional = productService.findStoreById(storeId);

        if (storeOptional.isEmpty() || storeOptional.get().getPix() == null || storeOptional.get().getPix().isBlank()) {
            model.addAttribute("error", "A loja não está configurada para receber pagamentos via PIX.");
            return "errorTemplate";
        }

        Store store = storeOptional.get();

        try {
            // --- LÓGICA ATUALIZADA ---
            // Usando nossa própria classe para gerar o código
            String brCode = PixGenerator.generatePayload(
                    store.getPix(),
                    total,
                    store.getName(),
                    "SAO PAULO", // A cidade é obrigatória pelo padrão do BACEN
                    "***" // txid estático para pagamentos sem ID de transação único
            );

            model.addAttribute("store", store);
            model.addAttribute("totalAmount", total);
            model.addAttribute("brCode", brCode);

            return "checkout";

        } catch (Exception e) {
            log.error("Erro ao gerar o código PIX para a loja {}", storeId, e);
            model.addAttribute("error", "Ocorreu um erro ao gerar o código de pagamento PIX.");
            return "errorTemplate";
        }
    }


    // ===================================================================================
    // MÉTODOS DE VISUALIZAÇÃO PARA CLIENTES (INTOCADOS)
    // ===================================================================================

    @RequestMapping("/")
    public String redirectToDefaultStoreRoot() {
        return "redirect:/1/";
    }

    @GetMapping("/{storeId}/")
    public String showStoreRootPage(@PathVariable Long storeId, Model model) {
        Optional<Store> storeOptional = productService.findStoreById(storeId);

        if (storeOptional.isPresent()) {
            Store store = storeOptional.get();
            model.addAttribute("store", store);
            model.addAttribute("currentStoreId", storeId);
            log.info("Carregando página inicial de entrada para Loja ID {} ({})", storeId, store.getName());
            return "index";
        } else {
            model.addAttribute("error", "Loja não encontrada.");
            log.warn("Tentativa de acesso a loja inexistente com ID: {}", storeId);
            return "errorTemplate";
        }
    }


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

    @GetMapping("/products/search")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam("name") String name,
                                        @RequestParam("storeId") Long storeId) {
        int limit = 3;
        List<Product> foundProducts = productService.searchProductsByNameAndStoreIdWithLimit(name, storeId, limit);
        System.out.println("Busca por '" + name + "' na loja " + storeId + ": Encontrados " + foundProducts.size() + " produtos.");
        return foundProducts;
    }

    @GetMapping("/{storeId}/search")
    public String searchProductsInStore(@PathVariable Long storeId,
                                        @RequestParam("name") String name,
                                        Model model) {
        List<Product> foundProducts = productService.searchProductsByNameAndStoreId(name, storeId);
        model.addAttribute("products", foundProducts);
        model.addAttribute("currentStoreId", storeId);
        model.addAttribute("searchTerm", name);
        System.out.println("Busca na Loja " + storeId + " por '" + name + "': Encontrados " + foundProducts.size() + " produtos.");
        return "storeProductsTemplate";
    }

    @GetMapping("/{storeId}/cart")
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


    /**
     * API para buscar um produto pelo seu código de barras.
     * Acessível via: GET /api/products/barcode/{barcode}
     */
    @GetMapping("/api/products/barcode/{barcode}")
    @ResponseBody
    public ResponseEntity<?> findProductByBarcode(@PathVariable String barcode, @RequestParam Long storeId) {
        log.info("Buscando produto com código de barras '{}' na loja ID {}", barcode, storeId);

        // Primeiro, buscamos o produto pelo código de barras
        Optional<Product> productOptional = productService.findByCodigoDeBarras(barcode);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            // Verificação de segurança: o produto encontrado pertence à loja correta?
            if (product.getStore().getId().equals(storeId)) {
                return ResponseEntity.ok(product); // Retorna o produto se encontrado e na loja certa
            } else {
                // Produto existe, mas em outra loja. Para o usuário, é como se não existisse.
                return ResponseEntity.notFound().build();
            }
        } else {
            // Produto não encontrado no banco de dados
            return ResponseEntity.notFound().build();
        }
    }
}
package com.japaneixxx.pagueleve.controller;

import com.japaneixxx.pagueleve.model.Category;
import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.service.CategoryService;
import com.japaneixxx.pagueleve.service.ImageUploadService;
import com.japaneixxx.pagueleve.service.ProductService;
import com.japaneixxx.util.PixGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ImageUploadService imageUploadService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, ImageUploadService imageUploadService, CategoryService categoryService) {
        this.productService = productService;
        this.imageUploadService = imageUploadService;
        this.categoryService = categoryService;
    }

    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "oldPrice", required = false) Double oldPrice,
            @RequestParam(value = "featured", required = false, defaultValue = "false") boolean featured,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "codigoDeBarras", required = false) String codigoDeBarras,
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam("storeId") Long storeId) {

        Optional<Store> storeOptional = productService.findStoreById(storeId);
        if (storeOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Loja com ID " + storeId + " não encontrada. Faça o login novamente.");
        }

        try {
            String finalImageUrl;

            if (imageFile != null && !imageFile.isEmpty()) {
                finalImageUrl = imageUploadService.uploadImage(imageFile);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                finalImageUrl = imageUrl;
            } else {
                finalImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHICWZcFeQ7UuaU7N30-E4Vt1GaTYIU1DIEA&s";
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setOldPrice(oldPrice);
            product.setHighlighted(featured);
            product.setImageUrl(finalImageUrl);
            product.setCodigoDeBarras(codigoDeBarras);
            product.setStore(storeOptional.get());

            if (categoryName != null && !categoryName.isBlank()) {
                Category category = categoryService.findOrCreateCategory(categoryName, String.valueOf(storeId));
                product.setCategory(category);
            }

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

    @PostMapping("/api/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(
            @PathVariable Long productId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "oldPrice", required = false) Double oldPrice,
            @RequestParam(value = "featured", required = false, defaultValue = "false") boolean featured,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "codigoDeBarras", required = false) String codigoDeBarras,
            @RequestParam(value = "category", required = false) String categoryName,
            @RequestParam("storeId") Long storeId) {

        Optional<Product> existingProductOptional = productService.findProductByIdAndStoreIdForLojista(productId, storeId);
        if (existingProductOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado. Você não tem permissão para editar este produto.");
        }

        Product productToUpdate = existingProductOptional.get();

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String newImageUrl = imageUploadService.uploadImage(imageFile);
                productToUpdate.setImageUrl(newImageUrl);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                productToUpdate.setImageUrl(imageUrl);
            }

            productToUpdate.setName(name);
            productToUpdate.setDescription(description);
            productToUpdate.setPrice(price);
            productToUpdate.setOldPrice(oldPrice);
            productToUpdate.setHighlighted(featured);
            productToUpdate.setCodigoDeBarras(codigoDeBarras);

            if (categoryName != null && !categoryName.isBlank()) {
                Category category = categoryService.findOrCreateCategory(categoryName, String.valueOf(storeId));
                productToUpdate.setCategory(category);
            } else {
                productToUpdate.setCategory(null);
            }

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

    @PatchMapping("/api/products/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateProductStatus(@PathVariable Long id, @RequestParam boolean active) {
        try {
            Optional<Product> updatedProduct = productService.updateProductStatus(id, active);
            if (updatedProduct.isPresent()) {
                return ResponseEntity.ok().body("Status do produto atualizado com sucesso.");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erro ao atualizar status do produto {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao atualizar status do produto.");
        }
    }

    @GetMapping("/api/categories/{lojistaId}")
    @ResponseBody
    public ResponseEntity<List<Category>> getCategoriesByLojista(@PathVariable String lojistaId) {
        return ResponseEntity.ok(categoryService.findCategoriesByLojista(lojistaId));
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<List<String>> getAllDistinctCategories() {
        return ResponseEntity.ok(categoryService.findAllDistinctCategoryNames());
    }

    @GetMapping("/api/stores/{storeId}/products")
    @ResponseBody
    public ResponseEntity<?> getProductsForStore(@PathVariable Long storeId) {
        List<Product> products = productService.findAllProductsByStoreIdForLojista(storeId);
        return ResponseEntity.ok(products);
    }

    @DeleteMapping("/api/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @RequestParam Long storeId) {
        Optional<Product> productOptional = productService.findProductByIdAndStoreIdForLojista(productId, storeId);
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
            String brCode = PixGenerator.generatePayload(
                    store.getPix(),
                    total,
                    store.getName(),
                    "BELO HORIZONTE",
                    "***"
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
            List<Product> highlightedProducts = productService.findActiveHighlightedProductsByStoreId(storeId);

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
            productOptional = productService.findActiveProductByIdAndStoreId(productId, storeId);
            System.out.println("Buscando produto com ID: " + productId + " e StoreId: " + storeId);
        } else {
            productOptional = productService.findProductById(productId); // This might need review if products should always be tied to a store
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
        List<Product> products = productService.findAllActiveProductsByStoreId(storeId);
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
        List<Product> foundProducts = productService.searchActiveProductsByNameAndStoreIdWithLimit(name, storeId, limit);
        System.out.println("Busca por '" + name + "' na loja " + storeId + ": Encontrados " + foundProducts.size() + " produtos.");
        return foundProducts;
    }

    @GetMapping("/{storeId}/search")
    public String searchProductsInStore(@PathVariable Long storeId,
                                        @RequestParam("name") String name,
                                        Model model) {
        List<Product> foundProducts = productService.searchActiveProductsByNameAndStoreId(name, storeId);
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

    @GetMapping("/api/products/barcode/{barcode}")
    @ResponseBody
    public ResponseEntity<?> findProductByBarcode(@PathVariable String barcode, @RequestParam Long storeId) {
        log.info("Buscando produto com código de barras '{}' na loja ID {}", barcode, storeId);

        Optional<Product> productOptional = productService.findByCodigoDeBarras(barcode);

        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            if (product.getStore().getId().equals(storeId) && product.isActive()) { // Check if active
                return ResponseEntity.ok(product);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

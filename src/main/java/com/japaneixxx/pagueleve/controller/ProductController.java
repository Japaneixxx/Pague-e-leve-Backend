package com.japaneixxx.pagueleve.controller;

import com.japaneixxx.pagueleve.model.Category;
import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.service.CategoryService;
import com.japaneixxx.pagueleve.service.ImageUploadService;
import com.japaneixxx.pagueleve.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;
    private final ImageUploadService imageUploadService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService,
                             ImageUploadService imageUploadService,
                             CategoryService categoryService) {
        this.productService = productService;
        this.imageUploadService = imageUploadService;
        this.categoryService = categoryService;
    }

    // ── Redireciona raiz para o SPA ──────────────────────────────────────────────

    @RequestMapping("/")
    public String redirectToDefaultStoreRoot() {
        return "redirect:/#/loja/1/";
    }

    @GetMapping("/{storeId}/")
    public String showStoreRootPage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/";
    }

    @GetMapping("/{storeId}/home")
    public String showStoreHomePage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/home";
    }

    @GetMapping("/produto/{productId}")
    public String showProductDetails(
            @PathVariable Long productId,
            @RequestParam(name = "storeId", required = false) Long storeId) {
        if (storeId != null) {
            return "redirect:/#/loja/" + storeId + "/produto/" + productId;
        }
        return "redirect:/#/loja/1/produto/" + productId;
    }

    @GetMapping("/{storeId}/produtos")
    public String listProductsByStore(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/produtos";
    }

    @GetMapping("/{storeId}/search")
    public String searchProductsInStore(
            @PathVariable Long storeId,
            @RequestParam("name") String name) {
        return "redirect:/#/loja/" + storeId + "/produtos";
    }

    @GetMapping("/{storeId}/cart")
    public String showCartPage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/cart";
    }

    @GetMapping("/{storeId}/checkout")
    public String showCheckoutPage(@PathVariable Long storeId) {
        return "redirect:/#/loja/" + storeId + "/checkout";
    }

    @GetMapping("/lojista/produto/{productId}/editar")
    public String showEditProductForm(@PathVariable Long productId) {
        return "redirect:/#/lojista/editar/" + productId;
    }

    // ── API: Produtos (lojista) ───────────────────────────────────────────────────

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
            return ResponseEntity.badRequest()
                    .body("Loja com ID " + storeId + " não encontrada.");
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
                Category category = categoryService.findOrCreateCategory(
                        categoryName, String.valueOf(storeId));
                product.setCategory(category);
            }

            productService.saveProduct(product);
            return ResponseEntity.ok("Produto criado com sucesso!");

        } catch (IOException e) {
            log.error("Erro de IO ao fazer upload da imagem para '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar o arquivo de imagem.");
        } catch (Exception e) {
            log.error("Erro inesperado ao criar o produto '{}'", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocorreu um erro inesperado no servidor.");
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

        Optional<Product> existingProductOptional =
                productService.findProductByIdAndStoreIdForLojista(productId, storeId);
        if (existingProductOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado. Você não tem permissão para editar este produto.");
        }

        Product productToUpdate = existingProductOptional.get();

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                productToUpdate.setImageUrl(imageUploadService.uploadImage(imageFile));
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
                Category category = categoryService.findOrCreateCategory(
                        categoryName, String.valueOf(storeId));
                productToUpdate.setCategory(category);
            } else {
                productToUpdate.setCategory(null);
            }

            productService.saveProduct(productToUpdate);
            return ResponseEntity.ok("Produto atualizado com sucesso!");

        } catch (IOException e) {
            log.error("Erro de IO ao fazer upload da nova imagem para o produto {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar a nova imagem.");
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar o produto {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocorreu um erro inesperado no servidor.");
        }
    }

    @PatchMapping("/api/products/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateProductStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        try {
            Optional<Product> updated = productService.updateProductStatus(id, active);
            if (updated.isPresent()) {
                return ResponseEntity.ok("Status do produto atualizado com sucesso.");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erro ao atualizar status do produto {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao atualizar status do produto.");
        }
    }

    @DeleteMapping("/api/products/{productId}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long productId,
            @RequestParam Long storeId) {
        Optional<Product> productOptional =
                productService.findProductByIdAndStoreIdForLojista(productId, storeId);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acesso negado. Você não tem permissão para excluir este produto.");
        }
        try {
            productService.deleteProductById(productId);
            return ResponseEntity.ok("Produto excluído com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao tentar excluir o produto {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Não foi possível excluir o produto.");
        }
    }

    // ── API: Categorias ───────────────────────────────────────────────────────────

    @GetMapping("/api/categories/{lojistaId}")
    @ResponseBody
    public ResponseEntity<List<Category>> getCategoriesByLojista(
            @PathVariable String lojistaId) {
        return ResponseEntity.ok(categoryService.findCategoriesByLojista(lojistaId));
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public ResponseEntity<List<String>> getAllDistinctCategories() {
        return ResponseEntity.ok(categoryService.findAllDistinctCategoryNames());
    }

    // ── API: Produtos da loja (lojista) ───────────────────────────────────────────

    @GetMapping("/api/stores/{storeId}/products")
    @ResponseBody
    public ResponseEntity<?> getProductsForStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(productService.findAllProductsByStoreIdForLojista(storeId));
    }

    // ── API: Busca por nome ───────────────────────────────────────────────────────

    @GetMapping("/products/search")
    @ResponseBody
    public List<Product> searchProducts(
            @RequestParam("name") String name,
            @RequestParam("storeId") Long storeId) {
        return productService.searchActiveProductsByNameAndStoreIdWithLimit(name, storeId, 3);
    }

    // ── API: Produto por código de barras ─────────────────────────────────────────

    @GetMapping("/api/products/barcode/{barcode}")
    @ResponseBody
    public ResponseEntity<?> findProductByBarcode(
            @PathVariable String barcode,
            @RequestParam Long storeId) {
        log.info("Buscando produto com barcode '{}' na loja {}", barcode, storeId);
        Optional<Product> productOptional = productService.findByCodigoDeBarras(barcode);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            if (product.getStore().getId().equals(storeId) && product.isActive()) {
                return ResponseEntity.ok(product);
            }
        }
        return ResponseEntity.notFound().build();
    }
}
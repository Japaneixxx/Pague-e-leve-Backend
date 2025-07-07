package com.japaneixxx.pagueleve.repository;

import com.japaneixxx.pagueleve.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para gerenciar a entidade Product.
 * Utiliza Spring Data JPA para abstrair o acesso ao banco de dados.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Encontra todos os produtos associados a um ID de loja específico.
     * O Spring Data JPA mapeia 'StoreId' para a propriedade 'store.id' na entidade Product.
     *
     * @param storeId O ID da loja.
     * @return Uma lista de produtos da loja.
     */
    List<Product> findByStoreId(Long storeId);

    /**
     * Encontra um produto específico pelo seu ID e pelo ID da sua loja.
     *
     * @param id      O ID do produto.
     * @param storeId O ID da loja.
     * @return Um Optional contendo o produto, se encontrado.
     */
    Optional<Product> findByIdAndStoreId(Long id, Long storeId);

    /**
     * Encontra todos os produtos marcados como destaque (highlighted).
     *
     * @return Uma lista de produtos em destaque.
     */
    List<Product> findByHighlightedTrue();

    /**
     * Encontra todos os produtos em destaque de uma loja específica.
     *
     * @param storeId O ID da loja.
     * @return Uma lista de produtos em destaque da loja.
     */
    List<Product> findByStoreIdAndHighlightedTrue(Long storeId);

    /**
     * Busca produtos cujo nome contenha um termo, ignorando maiúsculas/minúsculas.
     *
     * @param name O termo de busca.
     * @return Uma lista de produtos correspondentes.
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Busca produtos em uma loja específica cujo nome contenha um termo, ignorando maiúsculas/minúsculas.
     *
     * @param storeId O ID da loja.
     * @param name    O termo de busca.
     * @return Uma lista de produtos correspondentes na loja.
     */
    List<Product> findByStoreIdAndNameContainingIgnoreCase(Long storeId, String name);

    /**
     * Busca produtos cujo nome contenha um termo, com paginação e ordenação.
     *
     * @param name     O termo de busca.
     * @param pageable Objeto de paginação (contém tamanho da página, página atual e ordenação).
     * @return Um objeto Page contendo os produtos e informações de paginação.
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Busca produtos em uma loja específica cujo nome contenha um termo, com paginação e ordenação.
     *
     * @param storeId  O ID da loja.
     * @param name     O termo de busca.
     * @param pageable Objeto de paginação.
     * @return Um objeto Page contendo os produtos e informações de paginação.
     */
    Page<Product> findByStoreIdAndNameContainingIgnoreCase(Long storeId, String name, Pageable pageable);


    /**
     * Busca um produto pelo seu código de barras.
     * @param codigoDeBarras O código de barras a ser procurado.
     * @return Um Optional contendo o produto, se encontrado.
     */
    Optional<Product> findByCodigoDeBarras(String codigoDeBarras);
}
package com.japaneixxx.pagueleve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn; // Importar
import jakarta.persistence.ManyToOne; // Importar
import jakarta.persistence.Table;
import jakarta.persistence.FetchType; // Importar FetchType (para EAGER)

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name ="Produto";
    @Column(name = "description", nullable = false)
    private String description = "1";
    @Column(name = "price", nullable = false)
    private double price = 0;
    @Column(name = "image_url")
    private String imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRHICWZcFeQ7UuaU7N30-E4Vt1GaTYIU1DIEA&s";

    // Antiga coluna store_id (Long) FOI REMOVIDA
    // @Column(name = "store_id", nullable = false)
    // private Long storeId;

    // NOVO: Relação ManyToOne (Muitos Produtos para Uma Loja)
    @ManyToOne(fetch = FetchType.EAGER) // Garante que a Store seja sempre carregada junto com o Produto
    @JoinColumn(name = "store_id", nullable = false) // Mapeia para a coluna 'store_id' no banco de dados como FK
    private Store store; // Campo que representa a Loja associada

    @Column(name = "destaque", nullable = false) // Coluna 'destaque' no BD
    private boolean highlighted = false;


    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageUrl() {
    }

    // Antigo Getter e Setter para storeId (Long) FOI REMOVIDO
    // public Long getStoreId() { ... }
    // public void setStoreId(Long storeId) { ... }

    // NOVO: Getter e Setter para o objeto Store
    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public void setDescription() {
        this.description = " ";
    }

    public void setPrice() {
        this.price = 0;
    }
}
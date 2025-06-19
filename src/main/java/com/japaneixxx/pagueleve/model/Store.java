package com.japaneixxx.pagueleve.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "lojas") // Mapeia para a tabela 'lojas' no banco de dados
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id") // A chave primária da tabela 'lojas'
    private Long id;

    @Column(name = "nome", nullable = false) // Coluna 'nome' no banco de dados
    private String name;

    @Column(name = "pix") // Coluna 'pix' no banco de dados (não obrigatória por padrão)
    private String pix;

    // Construtores
    public Store() {
    }

    public Store(Long id) {
        this.id = id;
    }

    public Store(String name) {
        this.name = name;
    }

    public Store(String name, String pix) {
        this.name = name;
        this.pix = pix;
    }

    // Getters e Setters
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

    public String getPix() {
        return pix;
    }

    public void setPix(String pix) {
        this.pix = pix;
    }
}
package com.japaneixxx.pagueleve;

import com.japaneixxx.pagueleve.model.Product;
import com.japaneixxx.pagueleve.model.Store;
import com.japaneixxx.pagueleve.repository.ProductRepository;
import com.japaneixxx.pagueleve.repository.StoreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.util.Optional;

@SpringBootApplication
public class PagueleveApplication {

	public static void main(String[] args) {
		SpringApplication.run(PagueleveApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(ProductRepository productRepository, StoreRepository storeRepository) {
		return (args) -> {
			// *** Limpeza explícita para garantir um ambiente de desenvolvimento limpo ***
			// Remova estas linhas em produção ou quando usar ferramentas de migração de DB (Flyway/Liquibase)
			System.out.println("Limpando tabelas para garantir inserção de dados de exemplo...");
			productRepository.deleteAll(); // Limpa todos os produtos
			storeRepository.deleteAll();   // Limpa todas as lojas


			String storeName1 = "Sacolão Lisboa - Loja 1";
			String storeName2 = "Minimercado da Esquina - Loja 2";

			Store store1;
			Store store2;

			// Bloco para garantir que as lojas existam ou sejam criadas
			Optional<Store> existingStore1 = storeRepository.findByName(storeName1);
			Optional<Store> existingStore2 = storeRepository.findByName(storeName2);

			if (existingStore1.isPresent()) {
				store1 = existingStore1.get();
				System.out.println("Loja '" + storeName1 + "' já existe com ID: " + store1.getId());
			} else {
				System.out.println("Criando Loja '" + storeName1 + "'...");
				Store newStore1 = new Store(storeName1, "11.222.333/0001-44"); // Adiciona PIX
				store1 = storeRepository.save(newStore1); // Salva e captura o objeto Store com o ID gerado
				System.out.println("Loja '" + storeName1 + "' criada com ID: " + store1.getId());
			}

			if (existingStore2.isPresent()) {
				store2 = existingStore2.get();
				System.out.println("Loja '" + storeName2 + "' já existe com ID: " + store2.getId());
			} else {
				System.out.println("Criando Loja '" + storeName2 + "'...");
				Store newStore2 = new Store(storeName2, "chavepixdomercado@email.com"); // Adiciona PIX
				store2 = storeRepository.save(newStore2); // Salva e captura o objeto Store com o ID gerado
				System.out.println("Loja '" + storeName2 + "' criada com ID: " + store2.getId());
			}

			// Bloco para inserir produtos, apenas se a tabela de produtos estiver vazia
			if (productRepository.count() == 0) {
				System.out.println("Adicionando produtos de exemplo ao banco de dados...");

				// Produto 1
				Product product1 = new Product();
				product1.setName("Cogumelo Shimeji");
				product1.setDescription("200g");
				product1.setPrice(12);
				product1.setImageUrl("https://cdn.awsli.com.br/1571/1571225/produto/63075122/shimeji_branc-s47xzrnw4j.jpg");
				product1.setStore(store1);
				product1.setHighlighted(true);
				productRepository.save(product1);

				// Produto 2
				Product product2 = new Product();
				product2.setName("Manteiga");
				product2.setDescription("500g");
				product2.setPrice(20);
				product2.setImageUrl("https://ibassets.com.br/ib.item.image.large/l-eeb1bc8cbb4d429f9c92ad7bb1cc2316.jpeg");
				product2.setStore(store1);
				product2.setHighlighted(false);
				productRepository.save(product2);

				// Produto 3 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product3 = new Product();
				product3.setName("Beterraba");
				product3.setDescription("100g");
				product3.setPrice(6);
				// product3.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product3.setStore(store1);
				product3.setHighlighted(true);
				productRepository.save(product3);

				// Produto 4 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product4 = new Product();
				product4.setName("Arroz");
				product4.setDescription("1kg");
				product4.setPrice(15);
				// product4.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product4.setStore(store1);
				product4.setHighlighted(false);
				productRepository.save(product4);

				// Produto 5 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product5 = new Product();
				product5.setName("Feijão");
				product5.setDescription("1kg");
				product5.setPrice(25);
				// product5.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product5.setStore(store1);
				product5.setHighlighted(false);
				productRepository.save(product5);

				// Produto 6 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product6 = new Product();
				product6.setName("Maçã");
				product6.setDescription("1kg");
				product6.setPrice(7);
				// product6.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product6.setStore(store1);
				product6.setHighlighted(true);
				productRepository.save(product6);

				// Produto 7 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product7 = new Product();
				product7.setName("Coca-Cola");
				product7.setDescription("1l");
				product7.setPrice(6);
				// product7.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product7.setStore(store1);
				product7.setHighlighted(false);
				productRepository.save(product7);

				// Produto 8 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product8 = new Product();
				product8.setName("Coca-Cola Zero");
				product8.setDescription("1l");
				product8.setPrice(7);
				// product8.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product8.setStore(store1);
				product8.setHighlighted(false);
				productRepository.save(product8);

				// Produto 9 - USARÁ O VALOR DEFAULT DE image_url DA CLASSE PRODUCT
				Product product9 = new Product();
				product9.setName("Coca Cola");
				product9.setDescription("2l");
				product9.setPrice(7);
				// product9.setImageUrl("..."); REMOVIDO: Usará o default da classe Product
				product9.setStore(store2); // Este produto pertence à Loja 2
				product9.setHighlighted(false);
				productRepository.save(product9);

				System.out.println("Produtos de exemplo adicionados.");
			} else {
				System.out.println("Produtos e lojas já existem no banco de dados. Ignorando a inserção de dados de exemplo.");
			}
		};
	}

}
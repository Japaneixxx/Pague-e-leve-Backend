// home.js

/**
 * @file home.js
 * @description Script para gerenciar o comportamento do botão "Finalizar Compra" na página home.html
 * e simular a existência de produtos no carrinho usando sessionStorage.
 */

// Função para simular o "preenchimento" ou "esvaziamento" do carrinho no sessionStorage.
// Você pode chamar esta função no console do navegador para testar diferentes cenários.
function simulateCartStatus(hasProducts) {
    if (hasProducts) {
        // Armazena um item no sessionStorage para indicar que há produtos.
        // O valor pode ser um array de produtos, ou apenas um booleano, dependendo do que você precisar.
        sessionStorage.setItem('cartItems', JSON.stringify([{ id: 1, name: 'Produto A', price: 10.00 }]));
        console.log('Carrinho SIMULADO com produtos no sessionStorage.');
    } else {
        // Remove o item para simular um carrinho vazio.
        sessionStorage.removeItem('cartItems');
        console.log('Carrinho SIMULADO vazio no sessionStorage.');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 1. Captura do Botão
    const finalizarCompraBtn = document.getElementById('carrinho');

    // Verifica se o botão existe antes de adicionar o event listener.
    if (finalizarCompraBtn) {
        // 2. Adição de um Event Listener
        finalizarCompraBtn.addEventListener('click', () => {
            console.log('Botão Finalizar Compra clicado!');

            // 3. Verificação no sessionStorage
            // Tenta obter os itens do carrinho do sessionStorage.
            const cartItems = sessionStorage.getItem('cartItems');

            // Verifica se cartItems existe e se não é uma string vazia/null,
            // e se, após o parsing, contém algum item (se for um array, por exemplo).
            let hasProducts = false;
            if (cartItems) {
                try {
                    const parsedCartItems = JSON.parse(cartItems);
                    // Assume que se for um array, a existência de produtos é verificada pelo seu comprimento.
                    // Se você armazenar apenas um booleano, a lógica seria 'parsedCartItems === true'.
                    if (Array.isArray(parsedCartItems) && parsedCartItems.length > 0) {
                        hasProducts = true;
                    }
                } catch (e) {
                    console.error('Erro ao fazer parse dos itens do carrinho do sessionStorage:', e);
                    // Se houver um erro de parsing, trata como sem produtos para segurança.
                    hasProducts = false;
                }
            }

            // 4. Redirecionamento
            if (hasProducts) {
                console.log('Produtos encontrados no sessionStorage. Redirecionando para cart.html...');
                window.location.href = 'cart.html';
            } else {
                console.log('Nenhum produto no sessionStorage. Redirecionando para emptycart.html...');
                window.location.href = 'emptycart.html';
            }
        });
    } else {
        console.error('Botão com ID "carrinho" não encontrado no DOM. Verifique o home.html.');
    }
});
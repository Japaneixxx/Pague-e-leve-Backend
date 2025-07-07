document.addEventListener('DOMContentLoaded', () => {
    const startScanBtn = document.getElementById('start-scan-btn');
    const stopScanBtn = document.getElementById('stop-scan-btn');
    const scannerUi = document.getElementById('scanner-ui');
    const messageContainer = document.getElementById('scan-result-message');
    const currentStoreId = document.getElementById('storeIdData').value;

    // Configuração do scanner
    const html5QrcodeScanner = new Html5Qrcode("barcode-scanner-container");

    const onScanSuccess = async (decodedText, decodedResult) => {
        // Para o scanner assim que um código é lido com sucesso
        html5QrcodeScanner.stop().then(() => {
            scannerUi.style.display = 'none';
            console.log(`Código lido: ${decodedText}`);
            messageContainer.innerHTML = `<div class="alert alert-info">Código ${decodedText} lido. Buscando produto...</div>`;

            // Chama nossa API para buscar o produto
            fetch(`/api/products/barcode/${decodedText}?storeId=${currentStoreId}`)
                .then(response => {
                    if (response.ok) {
                        return response.json();
                    }
                    if (response.status === 404) {
                        throw new Error('Produto não encontrado.');
                    }
                    throw new Error('Erro ao buscar o produto.');
                })
                .then(product => {
                    // Se encontrou, redireciona para a página do produto
                    messageContainer.innerHTML = `<div class="alert alert-success">Produto encontrado! Redirecionando...</div>`;
                    window.location.href = `/produto/${product.id}?storeId=${product.store.id}`;
                })
                .catch(error => {
                    console.error('Erro:', error);
                    messageContainer.innerHTML = `<div class="alert alert-danger">${error.message}</div>`;
                });
        });
    };

    const onScanFailure = (error) => {
        // A biblioteca chama isso constantemente, então não mostramos erro na tela, apenas no console.
        // console.warn(`Falha na leitura do código: ${error}`);
    };

    startScanBtn.addEventListener('click', () => {
        scannerUi.style.display = 'block';
        messageContainer.innerHTML = ''; // Limpa mensagens antigas
        // Inicia a câmera e o scanner
        html5QrcodeScanner.start(
            { facingMode: "environment" }, // Usa a câmera traseira
            {
                fps: 10, // Frames por segundo
                qrbox: { width: 250, height: 150 } // Tamanho da caixa de leitura
            },
            onScanSuccess,
            onScanFailure
        ).catch(err => {
            console.error("Não foi possível iniciar o scanner", err);
            scannerUi.style.display = 'none';
            messageContainer.innerHTML = `<div class="alert alert-danger">Não foi possível acessar a câmera. Verifique as permissões.</div>`;
        });
    });

    stopScanBtn.addEventListener('click', () => {
        html5QrcodeScanner.stop().then(() => {
            scannerUi.style.display = 'none';
            messageContainer.innerHTML = '';
            console.log("Scanner parado pelo usuário.");
        }).catch(err => {
            console.error("Falha ao parar o scanner.", err);
            scannerUi.style.display = 'none';
        });
    });
});
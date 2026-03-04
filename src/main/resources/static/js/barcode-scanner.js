document.addEventListener('DOMContentLoaded', () => {
    const startScanBtn     = document.getElementById('start-scan-btn');
    const stopScanBtn      = document.getElementById('stop-scan-btn');
    const mirrorBtn        = document.getElementById('mirror-scan-btn');
    const scannerUi        = document.getElementById('scanner-ui');
    const messageContainer = document.getElementById('scan-result-message');
    const currentStoreId   = document.getElementById('storeIdData').value;

    const html5QrcodeScanner = new Html5Qrcode("barcode-scanner-container");
    let mirrored = false;

    function applyMirrorState() {
        const video = document.querySelector('#barcode-scanner-container video');
        if (video) {
            video.style.transform = mirrored ? 'scaleX(-1)' : 'scaleX(1)';
        } else {
            setTimeout(applyMirrorState, 100);
        }
    }

    function updateMirrorBtn() {
        if (!mirrorBtn) return;
        mirrorBtn.classList.toggle('active', mirrored);
        mirrorBtn.title = mirrored ? 'Desespelhar câmera' : 'Espelhar câmera';
    }

    if (mirrorBtn) {
        mirrorBtn.addEventListener('click', () => {
            mirrored = !mirrored;
            applyMirrorState();
            updateMirrorBtn();
        });
    }

    const onScanSuccess = async (decodedText) => {
        html5QrcodeScanner.stop().then(() => {
            scannerUi.style.display = 'none';
            mirrored = false;
            updateMirrorBtn();
            messageContainer.innerHTML = `<div class="alert alert-info">Código ${decodedText} lido. Buscando produto...</div>`;
            fetch(`/api/products/barcode/${decodedText}?storeId=${currentStoreId}`)
                .then(r => { if (r.ok) return r.json(); throw new Error(r.status === 404 ? 'Produto não encontrado.' : 'Erro ao buscar o produto.'); })
                .then(product => {
                    messageContainer.innerHTML = `<div class="alert alert-success">Produto encontrado! Redirecionando...</div>`;
                    window.location.href = `/produto/${product.id}?storeId=${product.store.id}`;
                })
                .catch(err => { messageContainer.innerHTML = `<div class="alert alert-danger">${err.message}</div>`; });
        });
    };

    startScanBtn.addEventListener('click', () => {
        scannerUi.style.display = 'block';
        messageContainer.innerHTML = '';
        mirrored = false;
        updateMirrorBtn();
        html5QrcodeScanner.start(
            { facingMode: "environment" },
            { fps: 10, qrbox: { width: 250, height: 150 } },
            onScanSuccess,
            () => {}
        ).catch(err => {
            scannerUi.style.display = 'none';
            messageContainer.innerHTML = `<div class="alert alert-danger">Não foi possível acessar a câmera. Verifique as permissões.</div>`;
        });
    });

    stopScanBtn.addEventListener('click', () => {
        html5QrcodeScanner.stop().then(() => {
            scannerUi.style.display = 'none';
            messageContainer.innerHTML = '';
            mirrored = false;
            updateMirrorBtn();
        }).catch(() => { scannerUi.style.display = 'none'; });
    });
});
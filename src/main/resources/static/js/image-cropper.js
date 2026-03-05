/**
 * image-cropper.js — Pague e Leve
 * Depende do Cropper.js (carregar via CDN antes deste script)
 * Uso: ImageCropper.init('inputId', 'cropperModal', (blob, filename) => { ... });
 */
const ImageCropper = (() => {

    let cropperInstance  = null;
    let originalFilename = 'imagem.jpg';
    let onConfirmCb      = null;
    let activeModalId    = null;

    function openModal(file, modalId) {
        originalFilename = file.name;
        activeModalId    = modalId;
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.getElementById('cropperImage');
            img.src = e.target.result;
            if (cropperInstance) { cropperInstance.destroy(); cropperInstance = null; }
            document.getElementById(modalId).classList.add('open');
            requestAnimationFrame(() => {
                cropperInstance = new Cropper(img, {
                    aspectRatio: NaN,
                    viewMode: 1,
                    dragMode: 'move',
                    autoCropArea: 0.85,
                    responsive: true,
                    restore: false,
                    guides: true,
                    center: true,
                    highlight: false,
                    cropBoxMovable: true,
                    cropBoxResizable: true,
                    toggleDragModeOnDblclick: false,
                });
            });
        };
        reader.readAsDataURL(file);
    }

    function closeModal() {
        if (activeModalId) document.getElementById(activeModalId)?.classList.remove('open');
        if (cropperInstance) { cropperInstance.destroy(); cropperInstance = null; }
    }

    function confirmCrop() {
        if (!cropperInstance) return;
        const canvas = cropperInstance.getCroppedCanvas({
            maxWidth: 1200, maxHeight: 1200,
            imageSmoothingEnabled: true,
            imageSmoothingQuality: 'high',
        });
        canvas.toBlob((blob) => {
            if (onConfirmCb) onConfirmCb(blob, originalFilename);
            closeModal();
        }, 'image/jpeg', 0.88);
    }

    function init(inputId, modalId, onConfirm) {
        onConfirmCb = onConfirm;
        const input = document.getElementById(inputId);
        if (!input) return;

        input.addEventListener('change', (e) => {
            const file = e.target.files[0];
            if (!file || !file.type.startsWith('image/')) return;
            openModal(file, modalId);
            input.value = '';
        });

        const bind = (id, fn) => document.getElementById(id)?.addEventListener('click', fn);
        bind('cropConfirmBtn',  confirmCrop);
        bind('cropCancelBtn',   closeModal);
        bind('cropRatioFree',   () => cropperInstance?.setAspectRatio(NaN));
        bind('cropRatio1x1',    () => cropperInstance?.setAspectRatio(1));
        bind('cropRatio4x3',    () => cropperInstance?.setAspectRatio(4 / 3));
        bind('cropRatio16x9',   () => cropperInstance?.setAspectRatio(16 / 9));
        bind('cropRotateLeft',  () => cropperInstance?.rotate(-90));
        bind('cropRotateRight', () => cropperInstance?.rotate(90));
        bind('cropFlipX',       () => { if (!cropperInstance) return; const d = cropperInstance.getData(); cropperInstance.scaleX(d.scaleX === -1 ? 1 : -1); });

        document.getElementById(modalId)?.addEventListener('click', (e) => {
            if (e.target.id === modalId) closeModal();
        });
    }

    return { init, openWithFile: openModal };
})();
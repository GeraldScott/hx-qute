// Handle HTMX modal integration with daisyUI
document.body.addEventListener('htmx:afterSwap', function(evt) {
    if (evt.detail.target.id === 'modal-container' ||
        evt.detail.target.classList.contains('modal-box')) {
        document.getElementById('modal-container').showModal();
    }
});

/* Completa el formulario de inicio de sesión al elegir una cuenta de demostración. */
document.addEventListener('DOMContentLoaded', () => {
    const codigo = document.getElementById('codigo');
    const password = document.getElementById('password');
    if (!codigo || !password) {
        return;
    }
    document.querySelectorAll('.cuenta-demo').forEach((cuenta) => {
        cuenta.addEventListener('click', () => {
            codigo.value = cuenta.dataset.codigo;
            password.value = cuenta.dataset.clave;
            password.focus();
        });
    });
});

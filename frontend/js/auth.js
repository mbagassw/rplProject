const authContainer = document.getElementById('auth-container');
const appContainer = document.getElementById('app-container');
const loginCard = document.getElementById('login-card');
const registerCard = document.getElementById('register-card');

// Navigasi Form Perpindahan Auth
document.getElementById('to-register')?.addEventListener('click', (e) => { e.preventDefault(); loginCard.classList.add('hidden'); registerCard.classList.remove('hidden'); });
document.getElementById('to-login')?.addEventListener('click', (e) => { e.preventDefault(); registerCard.classList.add('hidden'); loginCard.classList.remove('hidden'); });

function initAuthSession() {
    const savedUser = localStorage.getItem("user_session");
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        showDashboard();
    } else {
        showAuthPage();
    }
}

function showDashboard() {
    authContainer.classList.add('hidden');
    appContainer.classList.remove('hidden');

    document.getElementById('nav-username').innerText = currentUser.username;
    document.getElementById('nav-role').innerText = currentUser.role;

    checkRoleAccessibility();

    if (typeof loadProducts === "function") loadProducts();
    if (typeof loadOrders === "function") loadOrders();
    if (currentUser.role === 'ADMIN' && typeof loadTotalUsers === "function") loadTotalUsers();
}

function showAuthPage() {
    appContainer.classList.add('hidden');
    authContainer.classList.remove('hidden');
}

// PROSES SUBMIT LOGIN (Sudah Sinkron dengan ID index.html)
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('login-username').value.trim();
    const passwordInput = document.getElementById('login-password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
            body: JSON.stringify({ username: usernameInput, password: passwordInput })
        });

        const responseText = await response.text();
        let data = {};
        try { data = JSON.parse(responseText); } catch (e) {}

        if (response.ok) {
            localStorage.setItem("user_session", JSON.stringify(data));
            currentUser = data;
            showDashboard();
            alert(`Selamat datang kembali, ${data.username}!`);
        } else {
            alert(`Spring Boot Menolak (Status ${response.status}): ${data.error || data.message || responseText}`);
        }
    } catch (error) {
        alert("Gagal koneksi total ke port 9090! Pastikan Spring Boot Anda sudah di-klik RUN kembali.");
    }
});

// PROSES SUBMIT REGISTER
document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('register-username').value.trim();
    const passwordInput = document.getElementById('register-password').value;
    const roleInput = document.getElementById('register-role').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: usernameInput, password: passwordInput, role: roleInput })
        });

        const data = await response.json();

        if (response.ok) {
            alert("Registrasi Akun Berhasil! Silakan login.");
            document.getElementById('register-form').reset();
            document.getElementById('to-login').click();
        } else {
            alert(data.error || "Gagal registrasi.");
        }
    } catch (error) {
        alert("Koneksi gagal saat mencoba registrasi.");
    }
});

async function loadTotalUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/auth/users`);
        if (response.ok) {
            const users = await response.json();
            document.getElementById('total-users-count').innerText = users.length;
        }
    } catch (error) {}
}

document.getElementById('btn-logout').addEventListener('click', () => {
    if (confirm("Apakah Anda yakin ingin keluar?")) {
        localStorage.removeItem("user_session");
        currentUser = null;
        cart = [];
        document.getElementById('login-form').reset();
        showAuthPage();
    }
});
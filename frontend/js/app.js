// Konfigurasi Endpoint Backend Spring Boot (Port 9090)
const API_BASE_URL = "http://localhost:9090";

// State Global Aplikasi
let currentUser = null;
let cart = [];

// Helper: Format Angka ke Rupiah
function formatRupiah(number) {
    return new Intl.NumberFormat('id-ID', {
        style: 'currency',
        currency: 'IDR',
        minimumFractionDigits: 0
    }).format(number);
}

// Helper: Atur Hak Akses Elemen Berdasarkan Role
function checkRoleAccessibility() {
    if (!currentUser) return;

    // Sembunyikan elemen admin-only jika bukan admin
    const adminElements = document.querySelectorAll('.admin-only');
    adminElements.forEach(element => {
        if (currentUser.role === 'ADMIN') {
            element.classList.remove('hidden');
        } else {
            element.classList.add('hidden');
        }
    });

    // Sembunyikan elemen user-only (Keranjang Belanja) jika yang login adalah ADMIN
    const userElements = document.querySelectorAll('.user-only');
    userElements.forEach(element => {
        if (currentUser.role === 'ADMIN') {
            element.classList.add('hidden'); // Menyembunyikan keranjang dari Admin
        } else {
            element.classList.remove('hidden'); // Menampilkan keranjang untuk User
        }
    });

    const orderTitle = document.getElementById('order-title-role');
    if (orderTitle) {
        orderTitle.innerText = currentUser.role === 'ADMIN' ? '📋 Kelola Semua Pesanan Masuk' : '📋 Riwayat Pesanan Saya';
    }
}

// Inisialisasi Sesi Saat Web Dimuat
document.addEventListener("DOMContentLoaded", () => {
    if (typeof initAuthSession === "function") {
        initAuthSession();
    }
});
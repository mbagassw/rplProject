// Ambil elemen DOM terkait Produk
const productGrid = document.getElementById('product-grid');
const productModal = document.getElementById('product-modal');
const productForm = document.getElementById('product-form');
const btnAddProduct = document.getElementById('btn-add-product');
const closeModal = document.getElementById('close-modal');

// Event Listener untuk mengontrol Modal (Pop-up Form)
if (btnAddProduct) {
    btnAddProduct.addEventListener('click', () => {
        document.getElementById('modal-title').innerText = "Tambah Produk Baru";
        productForm.reset();
        document.getElementById('product-id').value = ""; // Kosong artinya mode INSERT
        productModal.classList.remove('hidden');
    });
}

if (closeModal) {
    closeModal.addEventListener('click', () => productModal.classList.add('hidden'));
}

// FUNGSI 1: Ambil Katalog Produk dari Backend Spring Boot
async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/products`);
        if (!response.ok) throw new Error("Gagal mengambil data produk");

        const products = await response.json();
        renderProducts(products);
    } catch (error) {
        console.error(error);
        productGrid.innerHTML = `<p class="empty-text" style="color:red;">Gagal memuat katalog produk.</p>`;
    }
}

// FUNGSI 2: Gambar kartu produk ke dalam HTML secara dinamis
function renderProducts(products) {
    if (products.length === 0) {
        productGrid.innerHTML = `<p class="empty-text">Belum ada produk yang dijual.</p>`;
        return;
    }

    productGrid.innerHTML = ""; // Bersihkan kontainer lama

    products.forEach(product => {
        const card = document.createElement('div');
        card.className = 'product-card';

        // Kondisional Tombol: Jika ADMIN muncul tombol Aksi CRUD, jika USER muncul tombol Beli
        let actionButtons = "";
        if (currentUser.role === 'ADMIN') {
            actionButtons = `
                <button class="btn btn-warning btn-sm" onclick="openEditProductModal(${product.id}, '${product.name}', ${product.price}, ${product.stock})">Edit</button>
                <button class="btn btn-danger btn-sm" onclick="deleteProduct(${product.id})">Hapus</button>
            `;
        } else {
            actionButtons = `
                <button class="btn btn-primary btn-sm btn-block" ${product.stock === 0 ? 'disabled' : ''} onclick="addToCart(${product.id}, '${product.name}', ${product.price}, ${product.stock})">
                    ${product.stock === 0 ? 'Habis' : '🛒 Masuk Keranjang'}
                </button>
            `;
        }

        card.innerHTML = `
            <div>
                <h4>${product.name}</h4>
                <p class="product-price">${formatRupiah(product.price)}</p>
                <p class="product-stock">Sisa Stok: <strong>${product.stock}</strong></p>
            </div>
            <div class="product-actions" style="margin-top: 10px;">
                ${actionButtons}
            </div>
        `;
        productGrid.appendChild(card);
    });
}

// FUNGSI 3: Handler Submit Tambah / Edit Produk (ADMIN)
productForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = document.getElementById('product-id').value;
    const name = document.getElementById('product-name').value;
    const price = parseFloat(document.getElementById('product-price').value);
    const stock = parseInt(document.getElementById('product-stock').value);

    const productData = { name, price, stock };

    // Tentukan apakah PUT (Edit) atau POST (Tambah Baru)
    const url = id ? `${API_BASE_URL}/api/products/${id}` : `${API_BASE_URL}/api/products`;
    const method = id ? 'PUT' : 'POST';

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(productData)
        });

        if (response.ok) {
            alert(id ? "Produk berhasil diperbarui!" : "Produk baru berhasil ditambahkan!");
            productModal.classList.add('hidden');
            loadProducts(); // Segarkan tampilan katalog
        } else {
            alert("Gagal menyimpan produk. Periksa input data.");
        }
    } catch (error) {
        alert("Terjadi kesalahan koneksi saat menyimpan produk.");
    }
});

// FUNGSI 4: Membuka Form Mode Edit (ADMIN)
function openEditProductModal(id, name, price, stock) {
    document.getElementById('modal-title').innerText = "Edit Detail Produk";
    document.getElementById('product-id').value = id;
    document.getElementById('product-name').value = name;
    document.getElementById('product-price').value = price;
    document.getElementById('product-stock').value = stock;
    productModal.classList.remove('hidden');
}

// FUNGSI 5: Menghapus Produk (ADMIN)
async function deleteProduct(id) {
    if (confirm("Apakah Anda yakin ingin menghapus produk ini dari katalog?")) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/products/${id}`, { method: 'DELETE' });
            if (response.ok) {
                alert("Produk berhasil dihapus!");
                loadProducts();
            } else {
                alert("Gagal menghapus produk.");
            }
        } catch (error) {
            alert("Koneksi gagal.");
        }
    }
}
const cartItemsContainer = document.getElementById('cart-items');
const cartTotalLabel = document.getElementById('cart-total');
const btnCheckout = document.getElementById('btn-checkout');
const orderListContainer = document.getElementById('order-list');

// Helper: Memformat Tanggal bawaan Java (LocalDateTime) menjadi format Indonesia
function formatTanggal(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString('id-ID', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// =========================================================================
// 1. FITUR KERANJANG BELANJA (CART) WITH TYPING QUANTITY OPTION
// =========================================================================

function addToCart(id, name, price, maxStock) {
    if (currentUser && currentUser.role === 'ADMIN') {
        alert("Mode Admin: Anda tidak dapat melakukan transaksi belanja.");
        return;
    }

    const existingItem = cart.find(item => item.productId === id);

    if (existingItem) {
        if (existingItem.quantity >= maxStock) {
            alert(`Tidak bisa menambah! Stok maksimal produk '${name}' hanya ${maxStock} unit.`);
            return;
        }
        existingItem.quantity += 1;
    } else {
        cart.push({ productId: id, name: name, price: price, quantity: 1, maxStock: maxStock });
    }

    renderCart();
}

function updateCartQuantity(id, newQty) {
    const item = cart.find(i => i.productId === id);
    if (!item) return;

    let qty = newQty === "" ? 0 : parseInt(newQty);

    if (qty > item.maxStock) {
        alert(`Jumlah melebihi stok tersedia! Stok maksimal produk '${item.name}' adalah ${item.maxStock} unit.`);
        qty = item.maxStock;
        item.quantity = qty;
        renderCart();
        return;
    }

    item.quantity = qty;
    recalculateCartTotal();
}

function recalculateCartTotal() {
    let total = 0;
    cart.forEach(item => {
        const currentQty = (item.quantity < 1 || isNaN(item.quantity)) ? 0 : item.quantity;
        total += item.price * currentQty;
    });
    cartTotalLabel.innerText = formatRupiah(total);
}

function removeFromCart(id) {
    cart = cart.filter(item => item.productId !== id);
    renderCart();
}

function renderCart() {
    if (currentUser && currentUser.role === 'ADMIN') {
        cartItemsContainer.innerHTML = `<p class="empty-text" style="color: #718096; font-style: italic;">Mode Admin: Fitur keranjang dinonaktifkan.</p>`;
        cartTotalLabel.innerText = "Rp 0";
        btnCheckout.disabled = true;
        btnCheckout.style.display = "none";
        return;
    }

    if (cart.length === 0) {
        cartItemsContainer.innerHTML = `<p class="empty-text">Keranjang kosong. Silakan pilih produk.</p>`;
        cartTotalLabel.innerText = "Rp 0";
        btnCheckout.disabled = true;
        btnCheckout.style.display = "block";
        return;
    }

    cartItemsContainer.innerHTML = "";
    let total = 0;

    cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;

        const row = document.createElement('div');
        row.className = 'cart-item';
        row.style = "display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; border-bottom: 1px dashed #edf2f7; padding-bottom: 8px;";

        row.innerHTML = `
            <div style="flex-grow: 1;">
                <strong style="font-size: 13px; color:#2d3748;">${item.name}</strong><br>
                <div style="display: flex; align-items: center; gap: 6px; margin-top: 4px;">
                    <input type="number" 
                           value="${item.quantity}" 
                           min="0" 
                           max="${item.maxStock}" 
                           style="width: 55px; text-align: center; padding: 2px; font-size: 12px; border: 1px solid #cbd5e0; border-radius: 4px;"
                           oninput="updateCartQuantity(${item.productId}, this.value)"
                           onchange="renderCart()"> 
                    <small style="color: #718096;">x ${formatRupiah(item.price)}</small>
                </div>
            </div>
            <button class="btn btn-danger btn-sm" style="padding: 2px 6px; border-radius: 50%; width: 22px; height: 22px; display: flex; align-items: center; justify-content: center;" onclick="removeFromCart(${item.productId})">&times;</button>
        `;
        cartItemsContainer.appendChild(row);
    });

    recalculateCartTotal();
    btnCheckout.disabled = false;
    btnCheckout.style.display = "block";
}

btnCheckout.addEventListener('click', async () => {
    if (cart.length === 0) return;

    const adaKuantitasNol = cart.some(item => item.quantity <= 0 || isNaN(item.quantity));
    if (adaKuantitasNol) {
        alert("Quantity harus lebih dari 0!");
        return;
    }

    const orderRequestPayload = {
        userId: currentUser.id,
        items: cart.map(item => ({
            productId: item.productId,
            quantity: item.quantity
        }))
    };

    try {
        const response = await fetch(`${API_BASE_URL}/api/orders`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderRequestPayload)
        });

        const data = await response.json();

        if (response.ok) {
            alert("Transaksi Sukses! Pesanan Anda berhasil dibuat dengan status PENDING, Silahkan melakukan Pembayaran Nanti Di konfirmasi Admin.");
            cart = [];
            renderCart();
            loadProducts();
            loadOrders();
        } else {
            alert(data.error || "Gagal memproses pesanan.");
        }
    } catch (error) {
        alert("Koneksi gagal saat checkout.");
    }
});


// =========================================================================
// 2. FITUR MANAJEMEN ORDER / PESANAN
// =========================================================================

async function loadOrders() {
    try {
        const url = currentUser.role === 'ADMIN'
            ? `${API_BASE_URL}/api/orders`
            : `${API_BASE_URL}/api/orders/user/${currentUser.id}`;

        const response = await fetch(url);

        if (response.status === 404) {
            orderListContainer.innerHTML = `<p class="empty-text">Belum ada riwayat transaksi masuk.</p>`;
            updateAdminDashboardStats([]);
            return;
        }

        const orders = await response.json();
        renderOrders(orders);
    } catch (error) {
        console.error(error);
        orderListContainer.innerHTML = `<p class="empty-text" style="color:red;">Gagal memuat daftar pesanan.</p>`;
    }
}

function updateAdminDashboardStats(orders) {
    const adminDash = document.getElementById('admin-dashboard-summary');
    if (!adminDash) return;

    if (currentUser && currentUser.role === 'ADMIN' && orders && orders.length > 0) {
        let pendingCount = 0;
        let paidCount = 0;
        let shippedCount = 0;
        let rejectedCount = 0;
        let totalRevenue = 0;

        orders.forEach(order => {
            const status = order.status ? order.status.toUpperCase() : '';
            if (status === 'PENDING') pendingCount++;
            else if (status === 'PAID') { paidCount++; totalRevenue += order.totalPrice; }
            else if (status === 'SHIPPED') { shippedCount++; totalRevenue += order.totalPrice; }
            else if (status === 'REJECTED') rejectedCount++;
        });

        document.getElementById('stat-pending').innerText = pendingCount;
        document.getElementById('stat-paid').innerText = paidCount;
        document.getElementById('stat-shipped').innerText = shippedCount;
        document.getElementById('stat-rejected').innerText = rejectedCount;
        document.getElementById('stat-revenue').innerText = formatRupiah(totalRevenue);

        adminDash.classList.remove('hidden');
    } else {
        document.getElementById('stat-pending').innerText = "0";
        document.getElementById('stat-paid').innerText = "0";
        document.getElementById('stat-shipped').innerText = "0";
        document.getElementById('stat-rejected').innerText = "0";
        document.getElementById('stat-revenue').innerText = "Rp 0";
    }
}

function renderOrders(orders) {
    updateAdminDashboardStats(orders);

    if (!orders || orders.length === 0) {
        orderListContainer.innerHTML = `<p class="empty-text">Belum ada riwayat transaksi.</p>`;
        return;
    }

    orderListContainer.innerHTML = "";

    orders.forEach(order => {
        const card = document.createElement('div');
        card.className = 'order-card';

        const statusClass = `status-${order.status.toLowerCase()}`;
        const totalItemsCount = order.orderItems ? order.orderItems.reduce((acc, item) => acc + item.quantity, 0) : 0;
        const tanggalFormat = formatTanggal(order.orderDate);

        // FITUR BARU: Generate List Nama Barang yang Dibeli
        let itemsListMarkup = "";
        if (order.orderItems && order.orderItems.length > 0) {
            order.orderItems.forEach(item => {
                // Mengambil nama dari objek product bawaan relasi di Spring Boot
                const namaProduk = item.product ? item.product.name : "Produk Telah Dihapus";
                const hargaSatuan = item.product ? formatRupiah(item.product.price) : "Rp 0";

                itemsListMarkup += `
                    <div style="display: flex; justify-content: space-between; font-size: 12px; color: #4a5568; margin-bottom: 4px; padding-left: 6px; border-left: 2px solid #cbd5e0;">
                        <span>📦 ${namaProduk} <strong>(x${item.quantity})</strong></span>
                        <span style="color: #718096;">${hargaSatuan}</span>
                    </div>
                `;
            });
        } else {
            itemsListMarkup = `<p style="font-size: 11px; color: #e53e3e; font-style: italic;">Tidak ada detail item barang.</p>`;
        }

        let adminActionMarkup = "";
        if (currentUser.role === 'ADMIN') {
            adminActionMarkup = `
                <div style="margin-top: 12px; padding-top: 10px; border-top: 1px solid #edf2f7; display:flex; flex-direction:column; gap:8px;">
                    <div style="display:flex; align-items:center; justify-content:space-between;">
                        <small style="font-weight:bold; color:#4a5568;">Ubah Status:</small>
                        <select onchange="updateOrderStatus(${order.id}, this.value)" style="padding:4px; font-size:12px; border-radius:4px; border: 1px solid #cbd5e0;">
                            <option value="">-- Pilih --</option>
                            <option value="PENDING" ${order.status === 'PENDING' ? 'disabled' : ''}>PENDING</option>
                            <option value="PAID" ${order.status === 'PAID' ? 'disabled' : ''}>PAID</option>
                            <option value="SHIPPED" ${order.status === 'SHIPPED' ? 'disabled' : ''}>SHIPPED</option>
                            <option value="REJECTED" ${order.status === 'REJECTED' ? 'disabled' : ''}>REJECTED</option>
                        </select>
                    </div>
                    <button class="btn btn-danger btn-sm" 
                            style="width: 100%; padding: 6px; font-size: 11px; font-weight: bold; cursor: pointer; border-radius: 4px;" 
                            onclick="deleteOrder(${order.id})">
                        🗑️ Hapus Nota Pesanan
                    </button>
                </div>
            `;
        }

        card.innerHTML = `
            <div class="order-card-header">
                <span>ID Nota: #${order.id} ${currentUser.role === 'ADMIN' ? `[User: ${order.user?.username}]` : ''}</span>
                <span class="status-badge ${statusClass}">${order.status}</span>
            </div>
            <div class="order-card-body">
                <p style="font-size: 11px; color: #7f8c8d; margin-bottom: 8px;">📅 Waktu Order: ${tanggalFormat}</p>
                
                <div style="background: #f7fafc; padding: 8px; border-radius: 6px; margin-bottom: 8px; border: 1px solid #e2e8f0;">
                    <div style="font-size: 11px; font-weight: bold; color: #718096; margin-bottom: 4px; text-transform: uppercase;">Daftar Barang:</div>
                    ${itemsListMarkup}
                </div>

                <p>Total Belanja: <strong>${formatRupiah(order.totalPrice)}</strong></p>
                <p style="font-size:11px; color:#718096; margin-top:2px;">Jumlah Barang: ${totalItemsCount} item</p>
            </div>
            ${adminActionMarkup}
        `;
        orderListContainer.appendChild(card);
    });
}

async function updateOrderStatus(orderId, newStatus) {
    if (!newStatus) return;

    if (confirm(`Apakah Anda yakin ingin mengubah status Nota #${orderId} menjadi ${newStatus}?`)) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}/status?status=${newStatus}`, {
                method: 'PUT',
                headers: { 'accept': '*/*' }
            });

            const data = await response.json();

            if (response.ok) {
                alert(`Status Nota #${orderId} berhasil diupdate menjadi ${newStatus}!`);
                loadOrders();
            } else {
                alert(data.error || "Gagal mengubah status pesanan.");
                loadOrders();
            }
        } catch (error) {
            alert("Gagal terhubung ke server saat memperbarui status.");
        }
    } else {
        loadOrders();
    }
}

async function deleteOrder(orderId) {
    if (!confirm(`PERINGATAN: Apakah Anda yakin ingin menghapus Nota Pesanan #${orderId} secara permanen?`)) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/orders/${orderId}`, {
            method: 'DELETE'
        });

        let data;
        const textData = await response.text();
        try {
            data = JSON.parse(textData);
        } catch (e) {
            data = { message: textData };
        }

        if (response.ok) {
            alert(data.message || `Nota #${orderId} berhasil dihapus.`);
            loadOrders();
        } else {
            alert(data.message || "Gagal menghapus nota pesanan.");
        }
    } catch (error) {
        console.error("Error delete order:", error);
        alert("Gagal terhubung ke server.");
    }
}
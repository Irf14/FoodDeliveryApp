/* ============================================
   SAVORIA — Application Logic (Connected to Java Backend)
   ============================================ */

const API_BASE = "http://localhost:3000/api";

// ============================================
// STATE MANAGEMENT
// ============================================
const state = {
  currentUser: JSON.parse(localStorage.getItem('savoria_user')) || null,
  currentPage: localStorage.getItem('savoria_user') ? 'page-home' : 'page-auth',
  cart: JSON.parse(localStorage.getItem('savoria_cart')) || [],
  promoApplied: false,
  selectedRestaurant: null,
  restaurants: [],
  menuItems: [],
  orders: []
};

// Emoji map for food names
const foodEmojiMap = {
  'pizza': '🍕', 'burger': '🍔', 'chicken': '🍗', 'fries': '🍟', 'noodle': '🍜',
  'biryani': '🥘', 'tehari': '🥘', 'kacchi': '🍛', 'chocolate': '🍫', 'curry': '🍛', 
  'steak': '🥩', 'salad': '🥗', 'soup': '🍲', 'naan': '🫓', 'bread': '🍞', 
  'shrimp': '🦐', 'lobster': '🦞', 'cake': '🍰', 'ice': '🍦', 'drink': '🥤',
  'borhani': '🧋', 'dessert': '🍰'
};

const restaurantEmojiMap = {
  'pizza': '🍕', 'kfc': '🍗', 'burger': '🍔', 'hotel': '🏨', 'grill': '🔥'
};

const badgePhrases = ['🔥 Flava Town', '✨ Crowd Fav', '💎 Premium', '⭐ Top Rated', '🌶️ Spicy Pick'];

// ============================================
// HELPERS
// ============================================
function getFoodEmoji(name) {
  const lower = name.toLowerCase();
  for (const [key, emoji] of Object.entries(foodEmojiMap)) {
    if (lower.includes(key)) return emoji;
  }
  return '🍽️';
}

function getRestaurantEmoji(name) {
  const lower = name.toLowerCase();
  for (const [key, emoji] of Object.entries(restaurantEmojiMap)) {
    if (lower.includes(key)) return emoji;
  }
  return '🍴';
}

function showToast(icon, text) {
  const toast = document.getElementById('toast');
  document.getElementById('toast-icon').textContent = icon;
  document.getElementById('toast-text').textContent = text;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2500);
}

function updateCartBadges() {
  const count = state.cart.reduce((sum, c) => sum + parseInt(c.quantity), 0);
  document.querySelectorAll('[id^="cart-badge"]').forEach(el => { 
      el.textContent = count; 
      el.classList.add('badge-pulse');
      setTimeout(() => el.classList.remove('badge-pulse'), 500);
  });
  localStorage.setItem('savoria_cart', JSON.stringify(state.cart));
}

// ============================================
// API CALLS
// ============================================
async function apiPost(endpoint, data) {
  const body = new URLSearchParams(data).toString();
  const resp = await fetch(`${API_BASE}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: body
  });
  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}

async function apiGet(endpoint, params = {}) {
  const query = new URLSearchParams(params).toString();
  const resp = await fetch(`${API_BASE}${endpoint}${query ? '?' + query : ''}`);
  if (!resp.ok) throw new Error(await resp.text());
  return resp.json();
}

// ============================================
// PAGE NAVIGATION
// ============================================
async function showPage(pageId) {
  document.querySelectorAll('.page').forEach(p => p.classList.add('hidden'));
  document.getElementById(pageId).classList.remove('hidden');
  state.currentPage = pageId;
  window.scrollTo(0, 0);

  if (pageId === 'page-home') {
      fetchRestaurants();
      if (state.currentUser) {
          document.getElementById('user-display-name').textContent = state.currentUser.username;
      }
  }
  if (pageId === 'page-cart') renderCart();
  if (pageId === 'page-orders') fetchMyOrders();
  if (pageId === 'page-owner') fetchOwnerDashboard();
  
  updateCartBadges();
}

function toggleProfileModal() {
    const modal = document.getElementById('profile-modal');
    modal.classList.toggle('hidden');
    
    if (!modal.classList.contains('hidden') && state.currentUser) {
        document.getElementById('profile-name').textContent = state.currentUser.username;
        document.getElementById('profile-role').textContent = state.currentUser.role.replace('_', ' ');
        document.getElementById('profile-address').textContent = state.currentUser.address;
    }
}

// ============================================
// AUTHENTICATION
// ============================================
function toggleAuthTab(tab) {
  const tabs = document.querySelectorAll('.auth-tab');
  const loginDiv = document.getElementById('auth-login');
  const regDiv = document.getElementById('auth-register');
  const toggleText = document.getElementById('auth-toggle-text');

  if (tab === 'login') {
    tabs[0].classList.add('active'); tabs[1].classList.remove('active');
    loginDiv.classList.remove('hidden'); regDiv.classList.add('hidden');
    toggleText.innerHTML = "Don't have an account? <a onclick=\"toggleAuthTab('register')\">Register Now!</a>";
  } else {
    tabs[1].classList.add('active'); tabs[0].classList.remove('active');
    loginDiv.classList.add('hidden'); regDiv.classList.remove('hidden');
    toggleText.innerHTML = "Already have an account? <a onclick=\"toggleAuthTab('login')\">Login</a>";
  }
}

async function handleLogin() {
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value.trim();
  if (!username || !password) { showToast('⚠️', 'Please fill in all fields!'); return; }

  try {
    const user = await apiPost('/login', { username, password });
    state.currentUser = user;
    localStorage.setItem('savoria_user', JSON.stringify(user));
    showToast('🎉', 'Welcome back, ' + user.username + '!');
    
    if (user.role === 'RESTAURANT_OWNER' || user.role === 'ADMIN') {
        showPage('page-owner');
    } else {
        document.getElementById('user-display-name').textContent = user.username;
        showPage('page-home');
    }
  } catch (e) {
    showToast('❌', 'Login failed! Check credentials.');
  }
}

async function handleRegister() {
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value.trim();
  const address = document.getElementById('reg-address').value.trim();
  const role = document.getElementById('reg-role').value;
  if (!username || !password || !address) { showToast('⚠️', 'Please fill in all fields!'); return; }

  try {
    const user = await apiPost('/register', { username, password, address, role });
    state.currentUser = user;
    localStorage.setItem('savoria_user', JSON.stringify(user));
    showToast('✅', 'Account created! Welcome!');
    if (user.role === 'RESTAURANT_OWNER' || user.role === 'ADMIN') {
        showPage('page-owner');
    } else {
        document.getElementById('user-display-name').textContent = user.username;
        showPage('page-home');
    }
  } catch (e) {
    showToast('❌', 'Registration failed!');
  }
}

function handleLogout() {
  state.currentUser = null; state.cart = [];
  localStorage.removeItem('savoria_user');
  localStorage.removeItem('savoria_cart');
  showPage('page-auth');
}

// ============================================
// SEARCH & FEED
// ============================================
async function fetchRestaurants() {
  try {
    const restaurants = await apiGet('/restaurants');
    state.restaurants = restaurants;
    renderRestaurantFeed(restaurants);
  } catch (e) { console.error(e); }
}

function renderRestaurantFeed(list) {
  const feed = document.getElementById('restaurant-feed');
  if (!list || list.length === 0) {
    feed.innerHTML = `<div class="empty-state">
                        <div class="empty-state-icon">🥨</div>
                        <h3>No results found!</h3>
                        <p>Try searching for something else like "Biryani" or "Burger".</p>
                      </div>`;
    return;
  }
  feed.innerHTML = list.map(r => `
    <div class="card card-clickable restaurant-card" onclick="openRestaurantMenu('${r.id}')">
      <div class="restaurant-card-img-placeholder">${getRestaurantEmoji(r.name)}</div>
      <div class="restaurant-card-body">
        <div class="restaurant-card-pills">
          <span class="pill ${r.open ? 'pill-green' : 'pill-red'}">${r.open ? 'Open' : 'Closed'}</span>
        </div>
        <h4 class="restaurant-card-name">${r.name}</h4>
        <div class="restaurant-card-meta"><span>📍 ${r.area}</span></div>
      </div>
    </div>`).join('');
}

async function handleSearch(value) {
    const query = value.toLowerCase().trim();
    if (!query) { 
        document.getElementById('food-search-results').classList.add('hidden');
        renderRestaurantFeed(state.restaurants); 
        return; 
    }

    try {
        // Search Food
        const foods = await apiGet('/search/food', { name: query });
        const foodSection = document.getElementById('food-search-results');
        const foodList = document.getElementById('food-results-list');

        if (foods.length > 0) {
            foodSection.classList.remove('hidden');
            foodList.innerHTML = foods.map(item => {
                const restaurant = state.restaurants.find(r => r.id === item.restaurantId);
                const restName = restaurant ? restaurant.name : 'Unknown Restaurant';
                const restArea = restaurant ? `📍 ${restaurant.area}` : '';
                const cartItem = state.cart.find(c => c.menuItemId === item.id);
                const qty = cartItem ? cartItem.quantity : 0;
                
                return `
                <div class="card menu-item-card" style="margin-bottom:12px; cursor: pointer;" onclick="openRestaurantMenu('${item.restaurantId}')">
                    <span class="menu-item-emoji">${getFoodEmoji(item.name)}</span>
                    <div class="menu-item-details">
                        <div style="font-size: 10px; color: var(--primary); font-weight: 800; text-transform: uppercase; margin-bottom: 2px;">${restName}</div>
                        <h4>${item.name}</h4>
                        <div class="price">৳${item.price}</div>
                        <div class="customization">${item.customizations} • ${restArea}</div>
                    </div>
                    <div class="qty-controls" onclick="event.stopPropagation()">
                        ${qty > 0 ? `
                            <button class="qty-btn" onclick="updateCartQty('${item.id}', -1, true)">-</button>
                            <span class="qty-val">${qty}</span>
                            <button class="qty-btn" onclick="updateCartQty('${item.id}', 1, true)">+</button>
                        ` : `
                            <button class="add-to-cart-btn" onclick="addToCartFromSearch('${item.id}', '${item.name}', ${item.price}, '${item.restaurantId}')">+ Add</button>
                        `}
                    </div>
                </div>`;
            }).join('');
        } else {
            foodSection.classList.add('hidden');
        }

        // Filter local restaurants
        const filteredRests = state.restaurants.filter(r => 
            r.name.toLowerCase().includes(query) || r.area.toLowerCase().includes(query)
        );
        renderRestaurantFeed(filteredRests);
    } catch (e) { console.error(e); }
}

// ============================================
// MENU
// ============================================
async function openRestaurantMenu(id) {
    const rest = state.restaurants.find(r => r.id === id);
    if (!rest) return;
    state.selectedRestaurant = rest;
    
    document.getElementById('menu-restaurant-name').textContent = rest.name;
    document.getElementById('menu-status-pill').textContent = rest.open ? 'Open' : 'Closed';
    
    try {
        const items = await apiGet('/menu', { restaurantId: id });
        const list = document.getElementById('menu-items-list');
        list.innerHTML = items.map(item => {
            const cartItem = state.cart.find(c => c.menuItemId === item.id);
            const qty = cartItem ? cartItem.quantity : 0;
            
            return `
            <div class="card menu-item-card">
                <span class="menu-item-emoji">${getFoodEmoji(item.name)}</span>
                <div class="menu-item-details">
                    <h4>${item.name}</h4>
                    <div class="price">৳${item.price}</div>
                    <div class="customization">${item.customizations}</div>
                </div>
                <div class="qty-controls">
                    ${qty > 0 ? `
                        <button class="qty-btn" onclick="updateCartQty('${item.id}', -1)">-</button>
                        <span class="qty-val">${qty}</span>
                        <button class="qty-btn" onclick="updateCartQty('${item.id}', 1)">+</button>
                    ` : `
                        <button class="add-to-cart-btn" onclick="addToCart('${item.id}')">+ Add</button>
                    `}
                </div>
            </div>`;
        }).join('');
        state.menuItems = items;
        showPage('page-menu');
    } catch (e) { console.error(e); }
}

// ============================================
// CART
// ============================================
function addToCart(itemId) {
    const item = state.menuItems.find(m => m.id === itemId);
    if (!item) return;
    const existing = state.cart.find(c => c.menuItemId === itemId);
    if (existing) existing.quantity++;
    else state.cart.push({ menuItemId: item.id, name: item.name, quantity: 1, price: item.price, restaurantId: item.restaurantId });
    showToast('🛒', 'Added to cart!');
    updateCartBadges();
    
    // Refresh the current view
    if (state.currentPage === 'page-menu') openRestaurantMenu(state.selectedRestaurant.id);
    if (state.currentPage === 'page-home') handleSearch(document.getElementById('search-input').value);
}

function addToCartFromSearch(id, name, price, restId) {
    const existing = state.cart.find(c => c.menuItemId === id);
    if (existing) existing.quantity++;
    else state.cart.push({ menuItemId: id, name, quantity: 1, price, restaurantId: restId });
    showToast('🛒', 'Added to cart!');
    updateCartBadges();
    handleSearch(document.getElementById('search-input').value);
}

function updateCartQty(itemId, delta, fromSearch = false) {
    const cartItem = state.cart.find(c => c.menuItemId === itemId);
    if (!cartItem) return;
    
    cartItem.quantity += delta;
    if (cartItem.quantity <= 0) {
        state.cart = state.cart.filter(c => c.menuItemId !== itemId);
    }
    
    updateCartBadges();
    if (fromSearch) handleSearch(document.getElementById('search-input').value);
    else if (state.currentPage === 'page-menu') openRestaurantMenu(state.selectedRestaurant.id);
    else if (state.currentPage === 'page-cart') renderCart();
}

function renderCart() {
    const list = document.getElementById('cart-items-list');
    if (state.cart.length === 0) {
        document.getElementById('cart-empty').classList.remove('hidden');
        document.getElementById('cart-totals').classList.add('hidden');
        list.innerHTML = '';
        return;
    }
    document.getElementById('cart-empty').classList.add('hidden');
    document.getElementById('cart-totals').classList.remove('hidden');
    
    list.innerHTML = state.cart.map(c => `
        <div class="card cart-item">
            <span class="cart-item-emoji">${getFoodEmoji(c.name)}</span>
            <div class="cart-item-info"><h4>${c.name}</h4><p>${c.quantity} x ৳${c.price}</p></div>
            <button class="cart-item-remove" onclick="removeFromCart('${c.menuItemId}')">✕</button>
        </div>`).join('');
    
    const subtotal = state.cart.reduce((s, c) => s + (c.price * c.quantity), 0);
    const disc = state.promoApplied ? subtotal * 0.1 : 0;
    document.getElementById('cart-subtotal').textContent = '৳' + subtotal;
    document.getElementById('cart-total-final').textContent = '৳' + (subtotal + 30 - disc);
}

function removeFromCart(id) {
    state.cart = state.cart.filter(c => c.menuItemId !== id);
    renderCart();
    updateCartBadges();
}

function applyPromo() {
    const val = document.getElementById('promo-code-input').value;
    if (val.toUpperCase() === 'SAVE10') {
        state.promoApplied = true;
        showToast('🎉', '10% Discount Applied!');
        renderCart();
    } else { showToast('❌', 'Invalid Code'); }
}

async function placeOrder() {
    if (state.cart.length === 0) return;
    
    // Edge case: Verify first restaurant in cart is open
    const rest = state.restaurants.find(r => r.id === state.cart[0].restaurantId);
    if (rest && !rest.open) {
        showToast('🛑', 'This restaurant is currently closed!');
        return;
    }

    const itemsRaw = state.cart.map(c => `${c.menuItemId}:${c.name.replace(/:|\|/g,' ')}:${c.quantity}:${c.price}`).join('|');
    try {
        await apiPost('/order/place', {
            customerId: state.currentUser.id,
            restaurantId: state.cart[0].restaurantId,
            discountCode: state.promoApplied ? 'SAVE10' : '',
            items: itemsRaw
        });
        state.cart = []; state.promoApplied = false;
        showPage('page-success');
    } catch (e) { showToast('❌', 'Order failed!'); }
}

// ============================================
// ORDERS & AUTO-STATUS
// ============================================
async function fetchMyOrders() {
    try {
        const orders = await apiGet('/orders', { customerId: state.currentUser.id });
        const list = document.getElementById('orders-list');
        list.innerHTML = orders.map(o => `
            <div class="card order-track-card">
                <div class="order-track-header">
                    <h4>Order #${o.id.slice(-4)}</h4>
                    <span class="pill pill-orange">${o.status}</span>
                </div>
                <div class="order-track-progress">
                    <div class="progress-step ${['PLACED','PREPARING','DELIVERING','COMPLETED'].indexOf(o.status) >= 0 ? 'active' : ''}"></div>
                    <div class="progress-step ${['PREPARING','DELIVERING','COMPLETED'].indexOf(o.status) >= 0 ? 'active' : ''}"></div>
                    <div class="progress-step ${['DELIVERING','COMPLETED'].indexOf(o.status) >= 0 ? 'active' : ''}"></div>
                    <div class="progress-step ${['COMPLETED'].indexOf(o.status) >= 0 ? 'active' : ''}"></div>
                </div>
            </div>`).join('');
    } catch (e) { console.error(e); }
}

// Auto-advance orders (Demo Logic)
setInterval(async () => {
    if (!state.currentUser) return;
    try {
        const orders = await apiGet('/orders', { customerId: state.currentUser.id });
        for (const o of orders) {
            let next = '';
            if (o.status === 'PLACED') next = 'PREPARING';
            else if (o.status === 'PREPARING') next = 'DELIVERING';
            else if (o.status === 'DELIVERING') next = 'COMPLETED';
            
            if (next) {
                await apiPost('/order/status', { orderId: o.id, status: next });
            }
        }
        if (state.currentPage === 'page-orders') fetchMyOrders();
        if (state.currentPage === 'page-owner') fetchOwnerDashboard();
    } catch (e) {}
}, 60000); // Every 60 seconds

// ============================================
// OWNER
// ============================================
async function fetchOwnerDashboard() {
    try {
        const rests = await apiGet('/restaurants', { ownerId: state.currentUser.id });
        if (rests.length > 0) {
            state.ownerRestaurant = rests[0];
            document.getElementById('stat-total-orders').textContent = 'Live';
            renderOwnerMenu();
        }
    } catch (e) {}
}

async function renderOwnerMenu() {
    try {
        const items = await apiGet('/menu', { restaurantId: state.ownerRestaurant.id });
        const list = document.getElementById('owner-menu-list');
        list.innerHTML = items.map(item => `
            <div class="card menu-item-card">
                <span class="menu-item-emoji">${getFoodEmoji(item.name)}</span>
                <div class="menu-item-details">
                    <h4>${item.name}</h4>
                    <div class="price">৳${item.price} - Qty: ${item.quantity}</div>
                </div>
            </div>`).join('');
    } catch (e) {}
}

async function ownerAddMenuItem() {
    const name = document.getElementById('add-item-name').value;
    const price = document.getElementById('add-item-price').value;
    const qty = document.getElementById('add-item-qty').value;
    const custom = document.getElementById('add-item-custom').value;
    
    try {
        await apiPost('/menu/add', {
            restaurantId: state.ownerRestaurant.id,
            name, price, quantity: qty, customizations: custom
        });
        showToast('✅', 'Added to menu');
        renderOwnerMenu();
    } catch (e) { showToast('❌', 'Failed to add'); }
}

function switchOwnerTab(tab, el) {
    document.querySelectorAll('.owner-tab').forEach(t => t.classList.remove('active'));
    el.classList.add('active');
    document.querySelectorAll('[id^="owner-tab-"]').forEach(t => t.classList.add('hidden'));
    document.getElementById('owner-tab-' + tab).classList.remove('hidden');
}

// Init
document.addEventListener('DOMContentLoaded', () => {
    if (state.currentUser) {
        if (state.currentUser.role === 'RESTAURANT_OWNER' || state.currentUser.role === 'ADMIN') {
             showPage('page-owner');
        } else {
             document.getElementById('user-display-name').textContent = state.currentUser.username;
             showPage('page-home');
        }
    } else {
        showPage('page-auth');
    }
});

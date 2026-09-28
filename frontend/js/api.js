/**
 * Smart Library Resource Management & Automated Fine Calculator
 * Central API Client and Utilities
 */

const API_BASE = window.location.port === '8080' ? '/api' : 'http://localhost:8080/api';

const api = {
    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);

            // Handle No Content response
            if (response.status === 204) {
                return null;
            }

            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMsg = data && (data.message || data.error) ? (data.message || data.error) : `Error: HTTP ${response.status}`;
                throw new Error(errorMsg);
            }

            return data;
        } catch (err) {
            console.error(`API Call failed: ${url}`, err);
            throw err;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: body ? JSON.stringify(body) : undefined
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }
};

// Authentication state management
const auth = {
    isLoggedIn() {
        return !!localStorage.getItem('lib_auth_token');
    },

    getUser() {
        try {
            return JSON.parse(localStorage.getItem('lib_user') || '{}');
        } catch (e) {
            return {};
        }
    },

    setUser(user, token) {
        localStorage.setItem('lib_user', JSON.stringify(user));
        localStorage.setItem('lib_auth_token', token);
    },

    logout() {
        localStorage.removeItem('lib_user');
        localStorage.removeItem('lib_auth_token');
        window.location.href = 'index.html';
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = 'index.html';
        }
    }
};

// Toast notification helper
function showToast(message, type = 'info') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';
    if (type === 'warning') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Formatters
function formatCurrency(amount) {
    if (amount == null) return '₹0.00';
    return '₹' + Number(amount).toFixed(2);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    // Format YYYY-MM-DD or ISO string
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    const d = new Date(dateTimeStr);
    if (isNaN(d.getTime())) return dateTimeStr;
    return d.toLocaleDateString('en-IN', { 
        day: '2-digit', 
        month: 'short', 
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

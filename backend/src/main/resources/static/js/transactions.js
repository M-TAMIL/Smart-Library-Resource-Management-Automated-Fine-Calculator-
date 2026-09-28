/**
 * Transaction History Logic
 */
let allTransactions = [];

document.addEventListener('DOMContentLoaded', () => {
    auth.requireAuth();

    const user = auth.getUser();
    if (user.name) {
        const usernameEl = document.getElementById('displayUsername');
        const avatarEl = document.getElementById('avatarLetter');
        if (usernameEl) usernameEl.textContent = user.name;
        if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
    }

    document.getElementById('btnLogout')?.addEventListener('click', () => auth.logout());

    const searchInput = document.getElementById('txSearchInput');
    const typeFilter = document.getElementById('txTypeFilter');

    let debounceTimer;
    searchInput?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(loadTransactions, 300);
    });

    typeFilter?.addEventListener('change', loadTransactions);

    loadTransactions();
});

async function loadTransactions() {
    const search = document.getElementById('txSearchInput')?.value.trim();
    const type = document.getElementById('txTypeFilter')?.value;

    let endpoint = '/transactions';
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (type) params.append('type', type);
    if (params.toString()) {
        endpoint += '?' + params.toString();
    }

    try {
        const txs = await api.get(endpoint);
        allTransactions = txs || [];
        renderTransactionsTable(allTransactions);
    } catch (err) {
        showToast('Error loading transactions: ' + err.message, 'error');
    }
}

function renderTransactionsTable(transactions) {
    const tbody = document.getElementById('transactionsTableBody');
    if (!tbody) return;

    if (!transactions || transactions.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--slate-400); padding: 32px;">No transactions recorded</td></tr>`;
        return;
    }

    tbody.innerHTML = transactions.map(t => `
        <tr>
            <td><strong>#${t.transactionId}</strong></td>
            <td>
                <span class="badge ${getTransactionBadgeClass(t.transactionType)}">
                    ${formatTxType(t.transactionType)}
                </span>
            </td>
            <td><strong>${escapeHtml(t.description)}</strong></td>
            <td>${t.member ? escapeHtml(t.member.name) : '<span style="color:var(--slate-400);">-</span>'}</td>
            <td>${t.book ? escapeHtml(t.book.title) : '<span style="color:var(--slate-400);">-</span>'}</td>
            <td style="font-size: 12px; color: var(--slate-500);">${formatDateTime(t.transactionDate)}</td>
            <td><span class="badge ${t.status === 'SUCCESS' ? 'badge-available' : 'badge-danger'}">${t.status}</span></td>
        </tr>
    `).join('');
}

function getTransactionBadgeClass(type) {
    switch (type) {
        case 'BOOK_ISSUED': return 'badge-borrowed';
        case 'BOOK_RETURNED': return 'badge-available';
        case 'FINE_CREATED': return 'badge-danger';
        case 'FINE_PAID': return 'badge-paid';
        case 'BOOK_RESERVED': return 'badge-reserved';
        case 'MAINTENANCE_STARTED': return 'badge-maintenance';
        case 'MAINTENANCE_COMPLETED': return 'badge-available';
        case 'BOOK_ADDED': return 'badge-available';
        case 'BOOK_UPDATED': return 'badge-info';
        case 'BOOK_DELETED': return 'badge-danger';
        default: return 'badge-info';
    }
}

function formatTxType(type) {
    return (type || '').replace(/_/g, ' ');
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str).replace(/[&<>"']/g, function(m) {
        return {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[m];
    });
}

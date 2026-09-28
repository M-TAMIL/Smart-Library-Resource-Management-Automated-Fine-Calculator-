/**
 * Dashboard Logic - Fetches summary from /api/dashboard/summary
 */
document.addEventListener('DOMContentLoaded', () => {
    auth.requireAuth();

    const user = auth.getUser();
    if (user.name) {
        const usernameEl = document.getElementById('displayUsername');
        const avatarEl = document.getElementById('avatarLetter');
        if (usernameEl) usernameEl.textContent = user.name;
        if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
    }

    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => auth.logout());
    }

    const btnRefresh = document.getElementById('btnRefresh');
    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => {
            loadDashboard();
            showToast('Dashboard data refreshed', 'info');
        });
    }

    loadDashboard();
});

async function loadDashboard() {
    try {
        const data = await api.get('/dashboard/summary');
        if (!data) return;

        // Populate metrics
        document.getElementById('totalBooks').textContent = data.totalBooks ?? 0;
        document.getElementById('availableBooks').textContent = data.availableBooks ?? 0;
        document.getElementById('borrowedBooks').textContent = data.borrowedBooks ?? 0;
        document.getElementById('overdueBooks').textContent = data.overdueBooks ?? 0;
        document.getElementById('totalMembers').textContent = data.totalMembers ?? 0;
        document.getElementById('pendingFines').textContent = data.pendingFines ?? 0;
        document.getElementById('pendingFineAmount').textContent = formatCurrency(data.totalPendingFineAmount) + ' uncollected';
        document.getElementById('activeReservations').textContent = data.activeReservations ?? 0;
        document.getElementById('booksUnderMaintenance').textContent = data.booksUnderMaintenance ?? 0;

        // Render Recent Transactions
        const txTbody = document.getElementById('recentTransactionsBody');
        if (txTbody) {
            if (!data.recentTransactions || data.recentTransactions.length === 0) {
                txTbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--slate-400); padding: 24px;">No transactions recorded yet</td></tr>';
            } else {
                txTbody.innerHTML = data.recentTransactions.map(tx => `
                    <tr>
                        <td><span class="badge ${getTransactionBadge(tx.transactionType)}">${formatTxType(tx.transactionType)}</span></td>
                        <td><strong>${escapeHtml(tx.description)}</strong></td>
                        <td style="font-size: 12px; color: var(--slate-500);">${formatDateTime(tx.transactionDate)}</td>
                        <td><span class="badge ${tx.status === 'SUCCESS' ? 'badge-available' : 'badge-danger'}">${tx.status}</span></td>
                    </tr>
                `).join('');
            }
        }

        // Render Recent Alerts
        const alertBox = document.getElementById('recentAlertsContainer');
        if (alertBox) {
            if (!data.recentAlerts || data.recentAlerts.length === 0) {
                alertBox.innerHTML = '<p style="text-align:center; color: var(--slate-400); padding: 24px;">No unread alerts</p>';
            } else {
                alertBox.innerHTML = data.recentAlerts.map(a => `
                    <div class="alert-item ${a.readStatus === 'UNREAD' ? 'unread' : ''}">
                        <div class="alert-content">
                            <div class="alert-title">${getAlertIcon(a.alertType)} ${escapeHtml(a.alertType)}</div>
                            <div class="alert-msg">${escapeHtml(a.message)}</div>
                            <div class="alert-time">${formatDateTime(a.createdDate)}</div>
                        </div>
                    </div>
                `).join('');
            }
        }

    } catch (err) {
        showToast('Failed to load dashboard data: ' + err.message, 'error');
    }
}

function getTransactionBadge(type) {
    switch (type) {
        case 'BOOK_ISSUED': return 'badge-borrowed';
        case 'BOOK_RETURNED': return 'badge-available';
        case 'FINE_CREATED': return 'badge-danger';
        case 'FINE_PAID': return 'badge-paid';
        case 'BOOK_RESERVED': return 'badge-reserved';
        case 'MAINTENANCE_STARTED': return 'badge-maintenance';
        case 'MAINTENANCE_COMPLETED': return 'badge-available';
        default: return 'badge-info';
    }
}

function formatTxType(type) {
    return (type || '').replace(/_/g, ' ');
}

function getAlertIcon(type) {
    switch (type) {
        case 'OVERDUE': return '⚠️';
        case 'PENDING_FINE': return '💰';
        case 'RETURN_REMINDER': return '⏰';
        case 'RESERVED_AVAILABLE': return '🎉';
        case 'MAINTENANCE_COMPLETED': return '🛠️';
        default: return '📢';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>"']/g, function(m) {
        return {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[m];
    });
}

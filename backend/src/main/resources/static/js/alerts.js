/**
 * Alerts & Notifications Logic
 */
let allAlerts = [];

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

    document.getElementById('alertFilter')?.addEventListener('change', applyAlertFilter);
    document.getElementById('btnMarkAllRead')?.addEventListener('click', markAllAsRead);

    loadAlerts();
});

async function loadAlerts() {
    try {
        const alerts = await api.get('/alerts');
        allAlerts = alerts || [];
        applyAlertFilter();
    } catch (err) {
        showToast('Error loading alerts: ' + err.message, 'error');
    }
}

function applyAlertFilter() {
    const filter = document.getElementById('alertFilter')?.value;
    let filtered = allAlerts;

    if (filter === 'UNREAD') {
        filtered = allAlerts.filter(a => a.readStatus === 'UNREAD');
    } else if (filter) {
        filtered = allAlerts.filter(a => a.alertType === filter);
    }

    renderAlerts(filtered);
}

function renderAlerts(alerts) {
    const container = document.getElementById('alertsListContainer');
    if (!container) return;

    if (!alerts || alerts.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔔</div>
                <h3>No Alerts Found</h3>
                <p>Everything is up to date in the library circulation system.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = alerts.map(a => `
        <div class="alert-item ${a.readStatus === 'UNREAD' ? 'unread' : ''}">
            <div class="alert-content">
                <div class="alert-title">
                    <span style="font-size: 16px; margin-right: 4px;">${getAlertIcon(a.alertType)}</span>
                    <span class="badge ${getAlertBadge(a.alertType)}">${escapeHtml(a.alertType)}</span>
                    ${a.readStatus === 'UNREAD' ? '<span class="badge badge-danger" style="margin-left:6px; font-size:10px;">NEW</span>' : ''}
                </div>
                <div class="alert-msg" style="margin-top: 6px; font-size: 14px; font-weight: 500;">
                    ${escapeHtml(a.message)}
                </div>
                <div class="alert-time">
                    📅 Generated on ${formatDateTime(a.createdDate)}
                    ${a.member ? ` • Patron: <strong>${escapeHtml(a.member.name)}</strong>` : ''}
                    ${a.book ? ` • Title: <em>${escapeHtml(a.book.title)}</em>` : ''}
                </div>
            </div>
            <div>
                ${a.readStatus === 'UNREAD' ? `
                    <button class="btn btn-outline btn-sm" onclick="markAsRead(${a.alertId})">
                        Mark as Read
                    </button>
                ` : `
                    <span style="font-size: 12px; color: var(--slate-400);">Read</span>
                `}
            </div>
        </div>
    `).join('');
}

function getAlertIcon(type) {
    switch (type) {
        case 'OVERDUE': return '⚠️';
        case 'PENDING_FINE': return '💰';
        case 'RETURN_REMINDER': return '⏰';
        case 'RESERVED_AVAILABLE': return '🎉';
        case 'MAINTENANCE_COMPLETED': return '🛠️';
        case 'LOW_AVAILABILITY': return '📉';
        default: return '📢';
    }
}

function getAlertBadge(type) {
    switch (type) {
        case 'OVERDUE': return 'badge-danger';
        case 'PENDING_FINE': return 'badge-warning';
        case 'RESERVED_AVAILABLE': return 'badge-available';
        case 'MAINTENANCE_COMPLETED': return 'badge-info';
        default: return 'badge-maintenance';
    }
}

async function markAsRead(id) {
    try {
        await api.put(`/alerts/${id}/read`);
        showToast('Alert marked as read', 'info');
        await loadAlerts();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function markAllAsRead() {
    try {
        await api.put('/alerts/read-all');
        showToast('All alerts marked as read', 'success');
        await loadAlerts();
    } catch (err) {
        showToast(err.message, 'error');
    }
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

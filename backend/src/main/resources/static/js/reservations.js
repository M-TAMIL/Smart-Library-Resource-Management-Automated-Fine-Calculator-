/**
 * Book Reservation Logic
 */
let allReservations = [];

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

    document.getElementById('resStatusFilter')?.addEventListener('change', applyFilter);

    document.getElementById('btnOpenReserveModal')?.addEventListener('click', () => {
        loadModalDropdowns();
        openModal('reserveModal');
    });

    document.getElementById('reserveForm')?.addEventListener('submit', handleCreateReservation);

    loadReservations();
});

async function loadReservations() {
    try {
        const reservations = await api.get('/reservations');
        allReservations = reservations || [];
        applyFilter();
    } catch (err) {
        showToast('Error loading reservations: ' + err.message, 'error');
    }
}

async function loadModalDropdowns() {
    try {
        const [members, books] = await Promise.all([
            api.get('/members'),
            api.get('/books')
        ]);

        const memberSelect = document.getElementById('reserveMember');
        if (memberSelect) {
            const activeMembers = (members || []).filter(m => m.status === 'ACTIVE');
            memberSelect.innerHTML = '<option value="">-- Choose Member --</option>' +
                activeMembers.map(m => `
                    <option value="${m.memberId}">
                        ${escapeHtml(m.name)} (${m.department})
                    </option>
                `).join('');
        }

        const bookSelect = document.getElementById('reserveBook');
        if (bookSelect) {
            bookSelect.innerHTML = '<option value="">-- Choose Book --</option>' +
                (books || []).map(b => `
                    <option value="${b.bookId}">
                        ${escapeHtml(b.title)} [Available: ${b.availableQuantity}/${b.quantity}]
                    </option>
                `).join('');
        }
    } catch (err) {
        showToast('Failed to load members or books: ' + err.message, 'error');
    }
}

function applyFilter() {
    const status = document.getElementById('resStatusFilter')?.value;
    let filtered = allReservations;
    if (status) {
        filtered = allReservations.filter(r => r.status === status);
    }
    renderReservationsTable(filtered);
}

function renderReservationsTable(reservations) {
    const tbody = document.getElementById('reservationsTableBody');
    if (!tbody) return;

    if (!reservations || reservations.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--slate-400); padding: 32px;">No reservations found</td></tr>`;
        return;
    }

    tbody.innerHTML = reservations.map(r => `
        <tr>
            <td><strong>#${r.reservationId}</strong></td>
            <td>
                <strong style="color: var(--dark);">${escapeHtml(r.member.name)}</strong>
                <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(r.member.email)}</div>
            </td>
            <td>
                <strong style="color: var(--dark);">${escapeHtml(r.book.title)}</strong>
                <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(r.book.author)}</div>
            </td>
            <td>
                <span class="badge ${r.book.availableQuantity > 0 ? 'badge-available' : 'badge-danger'}">
                    ${r.book.availableQuantity} in stock
                </span>
            </td>
            <td>${formatDate(r.reservationDate)}</td>
            <td><strong>${formatDate(r.expiryDate)}</strong></td>
            <td><span class="badge ${getReservationBadge(r.status)}">${escapeHtml(r.status)}</span></td>
            <td>
                ${r.status === 'ACTIVE' ? `
                    <div style="display: flex; gap: 6px;">
                        <button class="btn btn-success btn-sm" onclick="completeReservation(${r.reservationId})" title="Fulfill Hold">
                            ✅ Picked Up
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="cancelReservation(${r.reservationId})" title="Cancel Hold">
                            ❌ Cancel
                        </button>
                    </div>
                ` : `
                    <span style="font-size: 12px; color: var(--slate-400);">-</span>
                `}
            </td>
        </tr>
    `).join('');
}

function getReservationBadge(status) {
    switch (status) {
        case 'ACTIVE': return 'badge-reserved';
        case 'COMPLETED': return 'badge-available';
        case 'CANCELLED': return 'badge-danger';
        case 'EXPIRED': return 'badge-maintenance';
        default: return 'badge-info';
    }
}

async function handleCreateReservation(e) {
    e.preventDefault();
    const memberId = parseInt(document.getElementById('reserveMember').value);
    const bookId = parseInt(document.getElementById('reserveBook').value);

    if (!memberId || !bookId) {
        showToast('Please select both a member and a book', 'warning');
        return;
    }

    const btn = document.getElementById('btnSubmitReserve');
    btn.disabled = true;
    btn.textContent = 'Reserving...';

    try {
        await api.post('/reservations', { memberId, bookId });
        showToast('Reservation registered! Member hold active.', 'success');
        closeModal('reserveModal');
        await loadReservations();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Confirm Reservation';
    }
}

async function completeReservation(id) {
    if (!confirm('Mark this reservation as COMPLETED (book picked up by member)?')) return;
    try {
        await api.put(`/reservations/${id}/complete`);
        showToast('Reservation marked as COMPLETED', 'success');
        await loadReservations();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function cancelReservation(id) {
    if (!confirm('Cancel this reservation?')) return;
    try {
        await api.put(`/reservations/${id}/cancel`);
        showToast('Reservation CANCELLED', 'info');
        await loadReservations();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function openModal(id) {
    document.getElementById(id)?.classList.add('active');
}

function closeModal(id) {
    document.getElementById(id)?.classList.remove('active');
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

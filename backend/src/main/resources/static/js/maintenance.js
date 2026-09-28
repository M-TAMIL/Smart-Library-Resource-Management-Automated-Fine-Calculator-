/**
 * Book Maintenance Logic
 */
let allMaintenance = [];

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

    document.getElementById('btnOpenMaintModal')?.addEventListener('click', () => {
        loadBooksForMaintenance();
        const nextWeek = new Date();
        nextWeek.setDate(nextWeek.getDate() + 7);
        const dateInput = document.getElementById('maintExpectedDate');
        if (dateInput) dateInput.value = nextWeek.toISOString().split('T')[0];
        openModal('startMaintModal');
    });

    document.getElementById('startMaintForm')?.addEventListener('submit', handleStartMaintenance);

    loadMaintenance();
});

async function loadMaintenance() {
    try {
        const data = await api.get('/maintenance');
        allMaintenance = data || [];
        renderMaintenanceTable(allMaintenance);
    } catch (err) {
        showToast('Error loading maintenance: ' + err.message, 'error');
    }
}

async function loadBooksForMaintenance() {
    try {
        const books = await api.get('/books');
        const bookSelect = document.getElementById('maintBook');
        if (bookSelect) {
            const availableBooks = (books || []).filter(b => b.availableQuantity > 0);
            bookSelect.innerHTML = '<option value="">-- Choose Book --</option>' +
                availableBooks.map(b => `
                    <option value="${b.bookId}">
                        ${escapeHtml(b.title)} (${b.isbn}) [Available: ${b.availableQuantity}]
                    </option>
                `).join('');
        }
    } catch (err) {
        showToast('Failed to load books: ' + err.message, 'error');
    }
}

function renderMaintenanceTable(list) {
    const tbody = document.getElementById('maintenanceTableBody');
    if (!tbody) return;

    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--slate-400); padding: 32px;">No maintenance records recorded</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(m => `
        <tr>
            <td><strong>#${m.maintenanceId}</strong></td>
            <td>
                <strong style="color: var(--dark);">${escapeHtml(m.book.title)}</strong>
                <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(m.book.isbn)}</div>
            </td>
            <td>${escapeHtml(m.reason)}</td>
            <td>${formatDate(m.startDate)}</td>
            <td>${formatDate(m.expectedCompletionDate)}</td>
            <td>${formatDate(m.actualCompletionDate)}</td>
            <td style="font-size: 12px; color: var(--slate-600);">${escapeHtml(m.remarks || '-')}</td>
            <td>
                <span class="badge ${m.status === 'COMPLETED' ? 'badge-available' : 'badge-warning'}">
                    ${m.status === 'UNDER_MAINTENANCE' ? '🛠️ UNDER REPAIR' : '✅ RESTORED'}
                </span>
            </td>
            <td>
                ${m.status === 'UNDER_MAINTENANCE' ? `
                    <button class="btn btn-success btn-sm" onclick="completeMaintenance(${m.maintenanceId}, '${escapeHtml(m.book.title)}')">
                        ✅ Complete & Restore
                    </button>
                ` : `
                    <span style="font-size: 12px; color: var(--slate-400);">Completed</span>
                `}
            </td>
        </tr>
    `).join('');
}

async function handleStartMaintenance(e) {
    e.preventDefault();
    const bookId = parseInt(document.getElementById('maintBook').value);
    const reason = document.getElementById('maintReason').value.trim();
    const expectedCompletionDate = document.getElementById('maintExpectedDate').value;
    const remarks = document.getElementById('maintRemarks').value.trim();

    if (!bookId) {
        showToast('Please select a book', 'warning');
        return;
    }

    const btn = document.getElementById('btnSubmitMaint');
    btn.disabled = true;
    btn.textContent = 'Processing...';

    try {
        await api.post('/maintenance', {
            bookId,
            reason,
            expectedCompletionDate,
            remarks
        });

        showToast('Book marked as UNDER MAINTENANCE. Available copies updated.', 'success');
        closeModal('startMaintModal');
        document.getElementById('startMaintForm').reset();
        await loadMaintenance();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Start Maintenance';
    }
}

async function completeMaintenance(id, title) {
    const remarks = prompt(`Enter completion remarks for "${title}" (e.g. Spine rebound, page 42 fixed):`, 'Repair completed. Good condition.');
    if (remarks === null) return;

    try {
        await api.put(`/maintenance/${id}/complete`, { remarks });
        showToast(`Maintenance for "${title}" completed! Book copy restored to available inventory.`, 'success');
        await loadMaintenance();
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

/**
 * Fine Management Logic
 */
let allFines = [];

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

    document.getElementById('fineStatusFilter')?.addEventListener('change', () => {
        applyFilter();
    });

    loadFines();
});

async function loadFines() {
    try {
        const fines = await api.get('/fines');
        allFines = fines || [];
        updateFineSummary(allFines);
        applyFilter();
    } catch (err) {
        showToast('Error loading fines: ' + err.message, 'error');
    }
}

function updateFineSummary(fines) {
    let pendingCount = 0;
    let pendingTotal = 0;
    let paidCount = 0;
    let paidTotal = 0;

    fines.forEach(f => {
        if (f.paymentStatus === 'PENDING') {
            pendingCount++;
            pendingTotal += f.fineAmount;
        } else if (f.paymentStatus === 'PAID') {
            paidCount++;
            paidTotal += f.fineAmount;
        }
    });

    document.getElementById('pendingFinesCount').textContent = pendingCount;
    document.getElementById('pendingFinesTotal').textContent = formatCurrency(pendingTotal) + ' outstanding';
    document.getElementById('paidFinesCount').textContent = paidCount;
    document.getElementById('paidFinesTotal').textContent = formatCurrency(paidTotal) + ' collected';
}

function applyFilter() {
    const status = document.getElementById('fineStatusFilter')?.value;
    let filtered = allFines;
    if (status) {
        filtered = allFines.filter(f => f.paymentStatus === status);
    }
    renderFinesTable(filtered);
}

function renderFinesTable(fines) {
    const tbody = document.getElementById('finesTableBody');
    if (!tbody) return;

    if (!fines || fines.length === 0) {
        tbody.innerHTML = `<tr><td colspan="10" style="text-align: center; color: var(--slate-400); padding: 32px;">No fine records matching status</td></tr>`;
        return;
    }

    tbody.innerHTML = fines.map(f => `
        <tr>
            <td><strong>#${f.fineId}</strong></td>
            <td>
                <strong style="color: var(--dark);">${escapeHtml(f.member.name)}</strong>
                <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(f.member.department)}</div>
            </td>
            <td>
                <strong>${escapeHtml(f.borrow ? f.borrow.book.title : 'N/A')}</strong>
            </td>
            <td>#${f.borrow ? f.borrow.borrowId : '-'}</td>
            <td><span class="badge badge-warning">${f.overdueDays} Days Late</span></td>
            <td><strong style="color: var(--danger); font-size: 14px;">${formatCurrency(f.fineAmount)}</strong></td>
            <td>${formatDate(f.fineDate)}</td>
            <td>${formatDate(f.paymentDate)}</td>
            <td>
                <span class="badge ${f.paymentStatus === 'PAID' ? 'badge-paid' : 'badge-danger'}">
                    ${f.paymentStatus === 'PAID' ? '✅ PAID' : '⏳ PENDING'}
                </span>
            </td>
            <td>
                ${f.paymentStatus === 'PENDING' ? `
                    <button class="btn btn-success btn-sm" onclick="payFine(${f.fineId}, ${f.fineAmount}, '${escapeHtml(f.member.name)}')">
                        💳 Mark Paid
                    </button>
                ` : `
                    <span style="font-size: 12px; color: var(--slate-400);">Settled</span>
                `}
            </td>
        </tr>
    `).join('');
}

async function payFine(fineId, amount, memberName) {
    if (!confirm(`Confirm receipt of fine payment of ${formatCurrency(amount)} from ${memberName}?`)) {
        return;
    }

    try {
        await api.put(`/fines/${fineId}/pay`);
        showToast(`Fine #${fineId} marked as PAID. Transaction recorded.`, 'success');
        await loadFines();
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

/**
 * Book Return & Automated Fine Calculation Logic
 */
let activeBorrows = [];
let activeRule = { finePerDay: 5.0, gracePeriod: 0, maximumFine: 100.0 };
let selectedBorrow = null;

document.addEventListener('DOMContentLoaded', async () => {
    auth.requireAuth();

    const user = auth.getUser();
    if (user.name) {
        const usernameEl = document.getElementById('displayUsername');
        const avatarEl = document.getElementById('avatarLetter');
        if (usernameEl) usernameEl.textContent = user.name;
        if (avatarEl) avatarEl.textContent = user.name.charAt(0).toUpperCase();
    }

    document.getElementById('btnLogout')?.addEventListener('click', () => auth.logout());

    // Search filter
    document.getElementById('returnSearchInput')?.addEventListener('input', (e) => {
        const q = e.target.value.toLowerCase().trim();
        const filtered = activeBorrows.filter(b => 
            b.member.name.toLowerCase().includes(q) || 
            b.book.title.toLowerCase().includes(q) ||
            String(b.borrowId).includes(q)
        );
        renderReturnTable(filtered);
    });

    // Date change in return modal updates live calculation
    document.getElementById('modalReturnDate')?.addEventListener('change', updateCalculationPreview);

    // Form submission
    document.getElementById('returnForm')?.addEventListener('submit', handleConfirmReturn);

    await loadFineRule();
    await loadActiveBorrows();

    // Check if query param borrowId was passed
    const urlParams = new URLSearchParams(window.location.search);
    const preselectedBorrowId = urlParams.get('borrowId');
    if (preselectedBorrowId) {
        openReturnModal(Number(preselectedBorrowId));
    }
});

async function loadFineRule() {
    try {
        const rule = await api.get('/fine-rules/active');
        if (rule) {
            activeRule = rule;
            const badge = document.getElementById('activeRuleBadge');
            if (badge) {
                badge.textContent = `Rule: ₹${activeRule.finePerDay}/day • Grace: ${activeRule.gracePeriod}d • Max: ₹${activeRule.maximumFine}`;
            }
        }
    } catch (err) {
        console.warn('Could not load active fine rule, using defaults', err);
    }
}

async function loadActiveBorrows() {
    try {
        const borrows = await api.get('/borrows/active');
        activeBorrows = borrows || [];
        renderReturnTable(activeBorrows);
    } catch (err) {
        showToast('Error loading active borrowings: ' + err.message, 'error');
    }
}

function renderReturnTable(borrows) {
    const tbody = document.getElementById('returnTableBody');
    if (!tbody) return;

    if (!borrows || borrows.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--slate-400); padding: 32px;">No active borrowings awaiting return</td></tr>`;
        return;
    }

    const todayStr = new Date().toISOString().split('T')[0];

    tbody.innerHTML = borrows.map(b => {
        const dueDate = new Date(b.dueDate);
        const today = new Date(todayStr);
        const diffTime = today - dueDate;
        const overdueDays = Math.max(0, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
        
        let estimatedFine = 0;
        if (overdueDays > (activeRule.gracePeriod || 0)) {
            const chargeableDays = overdueDays - (activeRule.gracePeriod || 0);
            estimatedFine = chargeableDays * (activeRule.finePerDay || 5.0);
            if (activeRule.maximumFine && estimatedFine > activeRule.maximumFine) {
                estimatedFine = activeRule.maximumFine;
            }
        }

        return `
            <tr>
                <td><strong>#${b.borrowId}</strong></td>
                <td>
                    <strong style="color: var(--dark);">${escapeHtml(b.member.name)}</strong>
                    <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(b.member.email)}</div>
                </td>
                <td>
                    <strong style="color: var(--dark);">${escapeHtml(b.book.title)}</strong>
                    <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(b.book.author)}</div>
                </td>
                <td>${formatDate(b.borrowDate)}</td>
                <td><strong>${formatDate(b.dueDate)}</strong></td>
                <td>
                    <span class="badge ${overdueDays > 0 ? 'badge-danger' : 'badge-available'}">
                        ${overdueDays > 0 ? `${overdueDays} Days Late` : 'On Time'}
                    </span>
                </td>
                <td>
                    <strong style="color: ${estimatedFine > 0 ? 'var(--danger)' : 'var(--success)'}; font-size: 14px;">
                        ${formatCurrency(estimatedFine)}
                    </strong>
                </td>
                <td>
                    <button class="btn btn-success btn-sm" onclick="openReturnModal(${b.borrowId})">
                        📥 Return Book
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function openReturnModal(borrowId) {
    const borrow = activeBorrows.find(b => b.borrowId === borrowId);
    if (!borrow) return;

    selectedBorrow = borrow;

    document.getElementById('returnBorrowId').value = borrow.borrowId;
    document.getElementById('modalBookTitle').textContent = borrow.book.title + ' (' + borrow.book.isbn + ')';
    document.getElementById('modalMemberName').textContent = borrow.member.name + ' (' + borrow.member.department + ')';
    document.getElementById('modalDueDate').textContent = formatDate(borrow.dueDate);

    // Set today as default return date
    const todayStr = new Date().toISOString().split('T')[0];
    document.getElementById('modalReturnDate').value = todayStr;

    updateCalculationPreview();
    openModal('returnModal');
}

function updateCalculationPreview() {
    if (!selectedBorrow) return;

    const returnDateVal = document.getElementById('modalReturnDate').value;
    if (!returnDateVal) return;

    const returnDate = new Date(returnDateVal);
    const dueDate = new Date(selectedBorrow.dueDate);

    const diffTime = returnDate.getTime() - dueDate.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    const overdueDays = Math.max(0, diffDays);

    let fine = 0;
    const grace = activeRule.gracePeriod || 0;
    const rate = activeRule.finePerDay || 5.0;
    const maxFine = activeRule.maximumFine || 100.0;

    const explanationEl = document.getElementById('calcExplanation');
    const fineAmountEl = document.getElementById('calcFineAmount');

    if (overdueDays <= 0) {
        explanationEl.textContent = 'Returned on or before due date. Fine: ₹0.00';
        fineAmountEl.textContent = '₹0.00';
        fineAmountEl.style.color = 'var(--success)';
    } else {
        const chargeable = Math.max(0, overdueDays - grace);
        fine = chargeable * rate;
        if (maxFine && fine > maxFine) {
            fine = maxFine;
            explanationEl.textContent = `${overdueDays} days late - grace period (${grace}d) = ${chargeable} days × ₹${rate} (Capped at Max Fine ₹${maxFine})`;
        } else {
            explanationEl.textContent = `${overdueDays} days late - grace period (${grace}d) = ${chargeable} days × ₹${rate}/day`;
        }
        fineAmountEl.textContent = formatCurrency(fine);
        fineAmountEl.style.color = 'var(--danger)';
    }
}

async function handleConfirmReturn(e) {
    e.preventDefault();
    const borrowId = document.getElementById('returnBorrowId').value;
    const returnDate = document.getElementById('modalReturnDate').value;

    const btn = document.getElementById('btnConfirmReturn');
    btn.disabled = true;
    btn.textContent = 'Processing Return...';

    try {
        const returnedBorrow = await api.put(`/borrows/${borrowId}/return`, {
            returnDate: returnDate
        });

        closeModal('returnModal');
        showToast(`Book successfully returned! Available copies restored.`, 'success');
        await loadActiveBorrows();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Confirm Check-In & Apply In Database';
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

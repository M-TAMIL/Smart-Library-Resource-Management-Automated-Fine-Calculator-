/**
 * Book Issue (Borrow) Desk Logic
 */
let membersList = [];
let booksList = [];

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

    // Initialize dates (Today and +14 days)
    const today = new Date();
    const dueDate = new Date();
    dueDate.setDate(today.getDate() + 14);

    const borrowDateInput = document.getElementById('borrowDate');
    const dueDateInput = document.getElementById('dueDate');

    if (borrowDateInput) borrowDateInput.value = today.toISOString().split('T')[0];
    if (dueDateInput) dueDateInput.value = dueDate.toISOString().split('T')[0];

    // Change due date automatically if borrow date changed
    borrowDateInput?.addEventListener('change', () => {
        const bd = new Date(borrowDateInput.value);
        if (!isNaN(bd.getTime())) {
            const nd = new Date(bd);
            nd.setDate(nd.getDate() + 14);
            dueDateInput.value = nd.toISOString().split('T')[0];
        }
    });

    // Dropdown change notices
    document.getElementById('selectMember')?.addEventListener('change', onMemberSelected);
    document.getElementById('selectBook')?.addEventListener('change', onBookSelected);

    // Form submit
    document.getElementById('issueBookForm')?.addEventListener('submit', handleIssueBook);

    loadDropdowns();
    loadActiveBorrows();
});

async function loadDropdowns() {
    try {
        const [members, books] = await Promise.all([
            api.get('/members'),
            api.get('/books')
        ]);

        membersList = members || [];
        booksList = books || [];

        // Member dropdown
        const memberSelect = document.getElementById('selectMember');
        if (memberSelect) {
            memberSelect.innerHTML = '<option value="">-- Choose Member --</option>' + 
                membersList.map(m => {
                    const isBlocked = m.status === 'BLOCKED';
                    return `<option value="${m.memberId}" ${isBlocked ? 'style="color:red;"' : ''}>
                        ${escapeHtml(m.name)} (${m.department} - ${m.membershipType}) [Status: ${m.status}]
                    </option>`;
                }).join('');
        }

        // Book dropdown
        const bookSelect = document.getElementById('selectBook');
        if (bookSelect) {
            bookSelect.innerHTML = '<option value="">-- Choose Book --</option>' + 
                booksList.map(b => {
                    const isOutOfStock = b.availableQuantity <= 0;
                    return `<option value="${b.bookId}" ${isOutOfStock ? 'style="color:red;"' : ''}>
                        ${escapeHtml(b.title)} by ${escapeHtml(b.author)} (Avail: ${b.availableQuantity}/${b.quantity}) [${b.shelfNumber || 'No Shelf'}]
                    </option>`;
                }).join('');
        }

    } catch (err) {
        showToast('Error loading issue options: ' + err.message, 'error');
    }
}

function onMemberSelected(e) {
    const memberId = Number(e.target.value);
    const notice = document.getElementById('memberNotice');
    if (!memberId) {
        notice.textContent = '';
        return;
    }

    const member = membersList.find(m => m.memberId === memberId);
    if (!member) return;

    if (member.status === 'BLOCKED') {
        notice.style.color = 'var(--danger)';
        notice.textContent = '⚠️ Member account is BLOCKED. Book checkout will be rejected by backend.';
    } else if (member.status === 'INACTIVE') {
        notice.style.color = 'var(--warning)';
        notice.textContent = '⚠️ Member account is INACTIVE.';
    } else {
        notice.style.color = 'var(--success)';
        notice.textContent = '✅ Member account ACTIVE & eligible for book borrowing.';
    }
}

function onBookSelected(e) {
    const bookId = Number(e.target.value);
    const notice = document.getElementById('bookNotice');
    if (!bookId) {
        notice.textContent = '';
        return;
    }

    const book = booksList.find(b => b.bookId === bookId);
    if (!book) return;

    if (book.availableQuantity <= 0) {
        notice.style.color = 'var(--danger)';
        notice.textContent = '⚠️ Zero available copies! Reserve this book instead.';
    } else if (book.status === 'MAINTENANCE') {
        notice.style.color = 'var(--warning)';
        notice.textContent = '⚠️ Book is currently in MAINTENANCE.';
    } else {
        notice.style.color = 'var(--success)';
        notice.textContent = `✅ ${book.availableQuantity} copies available on shelf ${book.shelfNumber || 'General'}.`;
    }
}

async function handleIssueBook(e) {
    e.preventDefault();
    const memberId = parseInt(document.getElementById('selectMember').value);
    const bookId = parseInt(document.getElementById('selectBook').value);
    const borrowDate = document.getElementById('borrowDate').value;
    const dueDate = document.getElementById('dueDate').value;

    if (!memberId || !bookId) {
        showToast('Please select both a member and a book', 'warning');
        return;
    }

    const btn = document.getElementById('btnIssueBook');
    btn.disabled = true;
    btn.textContent = 'Processing Checkout...';

    try {
        await api.post('/borrows', {
            memberId,
            bookId,
            borrowDate,
            dueDate
        });

        showToast('Book successfully issued to member!', 'success');
        document.getElementById('issueBookForm').reset();

        // Reset dates
        const today = new Date();
        const nextDue = new Date();
        nextDue.setDate(today.getDate() + 14);
        document.getElementById('borrowDate').value = today.toISOString().split('T')[0];
        document.getElementById('dueDate').value = nextDue.toISOString().split('T')[0];
        document.getElementById('memberNotice').textContent = '';
        document.getElementById('bookNotice').textContent = '';

        loadDropdowns();
        loadActiveBorrows();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = '📤 Issue Book to Member';
    }
}

async function loadActiveBorrows() {
    try {
        const borrows = await api.get('/borrows/active');
        const countBadge = document.getElementById('activeBorrowsCount');
        if (countBadge) countBadge.textContent = `${borrows ? borrows.length : 0} Active Loans`;

        const tbody = document.getElementById('borrowsTableBody');
        if (!tbody) return;

        if (!borrows || borrows.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; color: var(--slate-400); padding: 32px;">No active borrowings currently</td></tr>';
            return;
        }

        const now = new Date().toISOString().split('T')[0];

        tbody.innerHTML = borrows.map(b => {
            const isOverdue = b.dueDate < now;
            return `
                <tr>
                    <td><strong>#${b.borrowId}</strong></td>
                    <td>
                        <strong style="color: var(--dark);">${escapeHtml(b.member.name)}</strong>
                        <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(b.member.email)}</div>
                    </td>
                    <td>
                        <strong>${escapeHtml(b.book.title)}</strong>
                        <div style="font-size: 11px; color: var(--slate-500);">${escapeHtml(b.book.author)}</div>
                    </td>
                    <td>${formatDate(b.borrowDate)}</td>
                    <td><strong>${formatDate(b.dueDate)}</strong></td>
                    <td>
                        <span class="badge ${isOverdue ? 'badge-overdue' : 'badge-borrowed'}">
                            ${isOverdue ? '⚠️ OVERDUE' : '⏳ ACTIVE'}
                        </span>
                    </td>
                    <td>
                        <a href="return.html?borrowId=${b.borrowId}" class="btn btn-success btn-sm">
                            📥 Check In / Return
                        </a>
                    </td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        showToast('Error loading active borrows: ' + err.message, 'error');
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

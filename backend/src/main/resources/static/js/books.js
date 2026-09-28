/**
 * Book Management Logic
 */
let allBooks = [];

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

    // Search and filter listeners
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');

    let debounceTimer;
    searchInput?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(loadBooks, 300);
    });

    statusFilter?.addEventListener('change', loadBooks);

    // Modal triggers
    document.getElementById('btnOpenAddModal')?.addEventListener('click', () => {
        document.getElementById('addBookForm').reset();
        openModal('addBookModal');
    });

    // Form Submissions
    document.getElementById('addBookForm')?.addEventListener('submit', handleAddBook);
    document.getElementById('editBookForm')?.addEventListener('submit', handleEditBook);

    loadBooks();
});

async function loadBooks() {
    const search = document.getElementById('searchInput')?.value.trim();
    const status = document.getElementById('statusFilter')?.value;

    let endpoint = '/books';
    const params = new URLSearchParams();
    if (search) params.append('search', search);
    if (status) params.append('status', status);
    if (params.toString()) {
        endpoint += '?' + params.toString();
    }

    try {
        const books = await api.get(endpoint);
        allBooks = books || [];
        renderBooksTable(allBooks);
    } catch (err) {
        showToast('Error loading books: ' + err.message, 'error');
    }
}

function renderBooksTable(books) {
    const tbody = document.getElementById('booksTableBody');
    if (!tbody) return;

    if (!books || books.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--slate-400); padding: 32px;">No books matching criteria</td></tr>`;
        return;
    }

    tbody.innerHTML = books.map(b => `
        <tr>
            <td><strong>#${b.bookId}</strong></td>
            <td><code>${escapeHtml(b.isbn)}</code></td>
            <td>
                <div style="font-weight: 600; color: var(--dark);">${escapeHtml(b.title)}</div>
                <div style="font-size: 12px; color: var(--slate-500);">${escapeHtml(b.author)} ${b.publicationYear ? '(' + b.publicationYear + ')' : ''}</div>
            </td>
            <td><span class="badge badge-info">${escapeHtml(b.category || 'General')}</span></td>
            <td><strong>${escapeHtml(b.shelfNumber || 'N/A')}</strong></td>
            <td>${b.quantity}</td>
            <td>
                <strong style="color: ${b.availableQuantity > 0 ? 'var(--success)' : 'var(--danger)'};">
                    ${b.availableQuantity}
                </strong>
            </td>
            <td><span class="badge ${getBookBadgeClass(b.status)}">${escapeHtml(b.status)}</span></td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-outline btn-sm" onclick="editBook(${b.bookId})" title="Edit Details">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteBook(${b.bookId}, '${escapeHtml(b.title)}')" title="Delete Book">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function getBookBadgeClass(status) {
    switch (status) {
        case 'AVAILABLE': return 'badge-available';
        case 'BORROWED': return 'badge-borrowed';
        case 'MAINTENANCE': return 'badge-maintenance';
        case 'RESERVED': return 'badge-reserved';
        default: return 'badge-info';
    }
}

async function handleAddBook(e) {
    e.preventDefault();
    const body = {
        isbn: document.getElementById('isbn').value.trim(),
        title: document.getElementById('title').value.trim(),
        author: document.getElementById('author').value.trim(),
        category: document.getElementById('category').value.trim(),
        publisher: document.getElementById('publisher').value.trim(),
        publicationYear: parseInt(document.getElementById('publicationYear').value) || null,
        quantity: parseInt(document.getElementById('quantity').value) || 1,
        availableQuantity: parseInt(document.getElementById('quantity').value) || 1,
        shelfNumber: document.getElementById('shelfNumber').value.trim(),
        status: 'AVAILABLE'
    };

    try {
        await api.post('/books', body);
        showToast('Book successfully added to library catalog!', 'success');
        closeModal('addBookModal');
        loadBooks();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function editBook(id) {
    const book = allBooks.find(b => b.bookId === id);
    if (!book) return;

    document.getElementById('editBookId').value = book.bookId;
    document.getElementById('editIsbn').value = book.isbn || '';
    document.getElementById('editTitle').value = book.title || '';
    document.getElementById('editAuthor').value = book.author || '';
    document.getElementById('editCategory').value = book.category || '';
    document.getElementById('editPublisher').value = book.publisher || '';
    document.getElementById('editPublicationYear').value = book.publicationYear || '';
    document.getElementById('editQuantity').value = book.quantity ?? 1;
    document.getElementById('editAvailableQuantity').value = book.availableQuantity ?? 0;
    document.getElementById('editShelfNumber').value = book.shelfNumber || '';
    document.getElementById('editStatus').value = book.status || 'AVAILABLE';

    openModal('editBookModal');
}

async function handleEditBook(e) {
    e.preventDefault();
    const id = document.getElementById('editBookId').value;
    const body = {
        isbn: document.getElementById('editIsbn').value.trim(),
        title: document.getElementById('editTitle').value.trim(),
        author: document.getElementById('editAuthor').value.trim(),
        category: document.getElementById('editCategory').value.trim(),
        publisher: document.getElementById('editPublisher').value.trim(),
        publicationYear: parseInt(document.getElementById('editPublicationYear').value) || null,
        quantity: parseInt(document.getElementById('editQuantity').value) || 1,
        availableQuantity: parseInt(document.getElementById('editAvailableQuantity').value) || 0,
        shelfNumber: document.getElementById('editShelfNumber').value.trim(),
        status: document.getElementById('editStatus').value
    };

    try {
        await api.put(`/books/${id}`, body);
        showToast('Book details updated successfully!', 'success');
        closeModal('editBookModal');
        loadBooks();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteBook(id, title) {
    if (!confirm(`Are you sure you want to permanently delete book "${title}" (ID: ${id})?`)) {
        return;
    }

    try {
        await api.delete(`/books/${id}`);
        showToast(`Book "${title}" deleted`, 'success');
        loadBooks();
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

/**
 * Member Management Logic
 */
let allMembers = [];

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

    const searchInput = document.getElementById('memberSearchInput');
    let debounceTimer;
    searchInput?.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(loadMembers, 300);
    });

    document.getElementById('btnOpenAddMember')?.addEventListener('click', () => {
        document.getElementById('addMemberForm').reset();
        openModal('addMemberModal');
    });

    document.getElementById('addMemberForm')?.addEventListener('submit', handleAddMember);
    document.getElementById('editMemberForm')?.addEventListener('submit', handleEditMember);

    loadMembers();
});

async function loadMembers() {
    const search = document.getElementById('memberSearchInput')?.value.trim();
    let endpoint = '/members';
    if (search) {
        endpoint += '?search=' + encodeURIComponent(search);
    }

    try {
        const members = await api.get(endpoint);
        allMembers = members || [];
        renderMembersTable(allMembers);
    } catch (err) {
        showToast('Error loading members: ' + err.message, 'error');
    }
}

function renderMembersTable(members) {
    const tbody = document.getElementById('membersTableBody');
    if (!tbody) return;

    if (!members || members.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; color: var(--slate-400); padding: 32px;">No members found</td></tr>`;
        return;
    }

    tbody.innerHTML = members.map(m => `
        <tr>
            <td><strong>#${m.memberId}</strong></td>
            <td><strong style="color: var(--dark);">${escapeHtml(m.name)}</strong></td>
            <td>
                <div>✉️ ${escapeHtml(m.email)}</div>
                <div style="font-size: 12px; color: var(--slate-500);">📞 ${escapeHtml(m.phone || 'N/A')}</div>
            </td>
            <td><span class="badge badge-info">${escapeHtml(m.department)}</span></td>
            <td><strong>${escapeHtml(m.membershipType)}</strong></td>
            <td>${formatDate(m.registrationDate)}</td>
            <td><span class="badge ${getMemberBadgeClass(m.status)}">${escapeHtml(m.status)}</span></td>
            <td>
                <div style="display: flex; gap: 6px;">
                    <button class="btn btn-outline btn-sm" onclick="editMember(${m.memberId})" title="Edit Details">✏️</button>
                    <button class="btn btn-danger btn-sm" onclick="deleteMember(${m.memberId}, '${escapeHtml(m.name)}')" title="Delete Member">🗑️</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function getMemberBadgeClass(status) {
    switch (status) {
        case 'ACTIVE': return 'badge-available';
        case 'INACTIVE': return 'badge-maintenance';
        case 'BLOCKED': return 'badge-danger';
        default: return 'badge-info';
    }
}

async function handleAddMember(e) {
    e.preventDefault();
    const body = {
        name: document.getElementById('memberName').value.trim(),
        email: document.getElementById('memberEmail').value.trim(),
        phone: document.getElementById('memberPhone').value.trim(),
        department: document.getElementById('memberDepartment').value.trim(),
        membershipType: document.getElementById('membershipType').value,
        status: document.getElementById('memberStatus').value
    };

    try {
        await api.post('/members', body);
        showToast('Member successfully registered!', 'success');
        closeModal('addMemberModal');
        loadMembers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

function editMember(id) {
    const member = allMembers.find(m => m.memberId === id);
    if (!member) return;

    document.getElementById('editMemberId').value = member.memberId;
    document.getElementById('editMemberName').value = member.name || '';
    document.getElementById('editMemberEmail').value = member.email || '';
    document.getElementById('editMemberPhone').value = member.phone || '';
    document.getElementById('editMemberDept').value = member.department || '';
    document.getElementById('editMemberType').value = member.membershipType || 'Student';
    document.getElementById('editMemberStatus').value = member.status || 'ACTIVE';

    openModal('editMemberModal');
}

async function handleEditMember(e) {
    e.preventDefault();
    const id = document.getElementById('editMemberId').value;
    const body = {
        name: document.getElementById('editMemberName').value.trim(),
        email: document.getElementById('editMemberEmail').value.trim(),
        phone: document.getElementById('editMemberPhone').value.trim(),
        department: document.getElementById('editMemberDept').value.trim(),
        membershipType: document.getElementById('editMemberType').value,
        status: document.getElementById('editMemberStatus').value
    };

    try {
        await api.put(`/members/${id}`, body);
        showToast('Member details updated successfully!', 'success');
        closeModal('editMemberModal');
        loadMembers();
    } catch (err) {
        showToast(err.message, 'error');
    }
}

async function deleteMember(id, name) {
    if (!confirm(`Are you sure you want to delete member "${name}"?`)) {
        return;
    }

    try {
        await api.delete(`/members/${id}`);
        showToast(`Member "${name}" deleted`, 'success');
        loadMembers();
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

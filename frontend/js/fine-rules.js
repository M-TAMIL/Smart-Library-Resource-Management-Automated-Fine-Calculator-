/**
 * Fine Rules Logic
 */
let currentActiveRule = null;

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
    document.getElementById('fineRuleForm')?.addEventListener('submit', handleSaveRule);

    loadRules();
});

async function loadRules() {
    try {
        const [activeRule, allRules] = await Promise.all([
            api.get('/fine-rules/active'),
            api.get('/fine-rules')
        ]);

        if (activeRule) {
            currentActiveRule = activeRule;
            document.getElementById('ruleId').value = activeRule.ruleId;
            document.getElementById('finePerDay').value = activeRule.finePerDay;
            document.getElementById('gracePeriod').value = activeRule.gracePeriod;
            document.getElementById('maximumFine').value = activeRule.maximumFine;
            document.getElementById('membershipType').value = activeRule.membershipType || 'ALL';
        }

        renderRulesTable(allRules || []);
    } catch (err) {
        showToast('Error loading fine rules: ' + err.message, 'error');
    }
}

function renderRulesTable(rules) {
    const tbody = document.getElementById('rulesTableBody');
    if (!tbody) return;

    if (!rules || rules.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--slate-400); padding: 24px;">No configured rules</td></tr>`;
        return;
    }

    tbody.innerHTML = rules.map(r => `
        <tr>
            <td><strong>#${r.ruleId}</strong></td>
            <td><strong style="color: var(--primary);">${formatCurrency(r.finePerDay)}</strong> / day</td>
            <td>${r.gracePeriod} Days</td>
            <td>${formatCurrency(r.maximumFine)}</td>
            <td><span class="badge badge-info">${escapeHtml(r.membershipType || 'ALL')}</span></td>
            <td>
                <span class="badge ${r.status === 'ACTIVE' ? 'badge-available' : 'badge-maintenance'}">
                    ${r.status === 'ACTIVE' ? '🟢 CURRENT ACTIVE' : '⚪ INACTIVE'}
                </span>
            </td>
        </tr>
    `).join('');
}

async function handleSaveRule(e) {
    e.preventDefault();
    const ruleId = document.getElementById('ruleId').value;
    const body = {
        finePerDay: parseFloat(document.getElementById('finePerDay').value),
        gracePeriod: parseInt(document.getElementById('gracePeriod').value),
        maximumFine: parseFloat(document.getElementById('maximumFine').value),
        membershipType: document.getElementById('membershipType').value,
        status: 'ACTIVE'
    };

    const btn = document.getElementById('btnSaveRule');
    btn.disabled = true;
    btn.textContent = 'Saving Policy...';

    try {
        if (ruleId) {
            await api.put(`/fine-rules/${ruleId}`, body);
        } else {
            await api.post('/fine-rules', body);
        }
        showToast('Fine rule successfully updated & applied in MySQL!', 'success');
        await loadRules();
    } catch (err) {
        showToast(err.message, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = '💾 Save & Apply Rule In Backend';
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

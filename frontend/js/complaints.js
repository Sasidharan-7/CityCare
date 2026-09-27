/**
 * CityCare - Complaints Module
 * 
 * Handles: complaint submission, listing, filtering, detail view,
 * image upload, status updates, resolution proof.
 */

const Complaints = (() => {

  // ── Submit New Complaint ───────────────────────────────
  async function submitComplaint(formData) {
    try {
      const data = await App.apiCall('/complaints', {
        method: 'POST',
        body: formData, // FormData for multipart upload
      });

      App.Toast.success(`Complaint submitted successfully! ID: #${data.complaintId || data.id}`);
      return data;
    } catch (err) {
      App.Toast.error(err.message || 'Failed to submit complaint.');
      throw err;
    }
  }

  // ── Fetch Complaints (with optional filters) ──────────
  async function getComplaints(params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val) query.append(key, val);
    });
    const qs = query.toString();
    const endpoint = '/complaints' + (qs ? '?' + qs : '');
    return App.apiCall(endpoint);
  }

  // ── Fetch Single Complaint ─────────────────────────────
  async function getComplaint(id) {
    return App.apiCall(`/complaints/${id}`);
  }

  // ── Update Complaint Status ────────────────────────────
  async function updateStatus(id, status, remarks) {
    return App.apiCall(`/complaints/${id}/status`, {
      method: 'PUT',
      body: { status, remarks },
    });
  }

  // ── Upload Resolution Proof ────────────────────────────
  async function uploadResolutionProof(id, formData) {
    return App.apiCall(`/complaints/${id}/resolution-proof`, {
      method: 'POST',
      body: formData,
    });
  }

  // ── Fetch Officer's Assigned Complaints ────────────────
  async function getOfficerComplaints(params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, val]) => {
      if (val) query.append(key, val);
    });
    const qs = query.toString();
    return App.apiCall('/officer/complaints' + (qs ? '?' + qs : ''));
  }

  // ── Fetch Admin Analytics ──────────────────────────────
  async function getAnalytics() {
    return App.apiCall('/admin/analytics');
  }

  // ── Fetch Notifications ────────────────────────────────
  async function getNotifications() {
    return App.apiCall('/notifications');
  }

  // ── Mark Notification as Read ──────────────────────────
  async function markNotificationRead(id) {
    return App.apiCall(`/notifications/${id}/read`, { method: 'PUT' });
  }

  // ── Fetch Departments ──────────────────────────────────
  async function getDepartments() {
    return App.apiCall('/departments');
  }

  // ── Render Complaint Status Timeline ───────────────────
  function renderTimeline(status, updates = []) {
    const statuses = ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'];
    const statusIndex = statuses.indexOf(status);

    return statuses.map((s, i) => {
      let stateClass = '';
      if (i < statusIndex) stateClass = 'completed';
      else if (i === statusIndex) stateClass = 'active';

      // Find update entry for this status
      const update = updates.find(u => u.status === s);
      const dateStr = update ? App.formatDateTime(update.createdAt) : '';
      const remarks = update ? update.remarks : '';

      return `
        <div class="timeline-item ${stateClass}">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <h4>${App.statusLabel(s)}</h4>
            <p>${dateStr}${remarks ? ' — ' + App.escapeHtml(remarks) : ''}</p>
          </div>
        </div>
      `;
    }).join('');
  }

  // ── Render Complaint Card (for list) ───────────────────
  function renderComplaintCard(complaint, detailPage = 'complaint-details.html') {
    const imageUrl = complaint.imageUrl || 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80" fill="%23d1d5db" viewBox="0 0 24 24"><rect width="24" height="24" rx="4" fill="%23f3f4f6"/><path d="M4 16l4-4 4 4 4-6 4 6" stroke="%23d1d5db" fill="none" stroke-width="1.5"/></svg>';

    return `
      <div class="complaint-card" onclick="window.location.href='${detailPage}?id=${complaint.id}'">
        <img class="complaint-thumb" src="${imageUrl}" alt="${App.categoryLabel(complaint.category)}" onerror="this.style.display='none'">
        <div class="complaint-info">
          <div class="complaint-id">#${complaint.complaintId || complaint.id} · ${App.formatDate(complaint.createdAt)}</div>
          <div class="complaint-title">${App.categoryLabel(complaint.category)} — ${App.escapeHtml(complaint.description || '')}</div>
          <div class="complaint-meta">
            <span class="badge ${App.statusBadge(complaint.status)}">${App.statusLabel(complaint.status)}</span>
            <span class="badge ${App.priorityBadge(complaint.priority)}">${complaint.priority}</span>
          </div>
        </div>
      </div>
    `;
  }

  // ── Render Stats ───────────────────────────────────────
  function renderStatCard(icon, label, value, colorClass) {
    return `
      <div class="stat-card">
        <div class="stat-icon ${colorClass}">${icon}</div>
        <div class="stat-info">
          <h4>${label}</h4>
          <div class="stat-value">${value}</div>
        </div>
      </div>
    `;
  }

  // ── Bar Chart Renderer (CSS-only) ──────────────────────
  function renderBarChart(items, maxValue) {
    return items.map(item => {
      const pct = maxValue > 0 ? Math.round((item.value / maxValue) * 100) : 0;
      return `
        <div class="bar-chart-item">
          <div class="bar-label">${item.label}</div>
          <div class="bar-track">
            <div class="bar-fill ${item.color || 'blue'}" style="width: ${Math.max(pct, 2)}%">${item.value}</div>
          </div>
        </div>
      `;
    }).join('');
  }

  return {
    submitComplaint,
    getComplaints,
    getComplaint,
    updateStatus,
    uploadResolutionProof,
    getOfficerComplaints,
    getAnalytics,
    getNotifications,
    markNotificationRead,
    getDepartments,
    renderTimeline,
    renderComplaintCard,
    renderStatCard,
    renderBarChart,
  };
})();

/**
 * CityCare - Dashboard Module
 * 
 * Shared logic for citizen, officer, and admin dashboards.
 * Loads stats, complaint lists, maps, analytics.
 */

const Dashboard = (() => {

  // ── Load Citizen Dashboard ─────────────────────────────
  async function loadCitizenDashboard() {
    try {
      const complaints = await Complaints.getComplaints();
      const list = Array.isArray(complaints) ? complaints : (complaints.content || []);

      // Calculate stats
      const total = list.length;
      const pending = list.filter(c => c.status === 'PENDING' || c.status === 'ASSIGNED').length;
      const inProgress = list.filter(c => c.status === 'IN_PROGRESS').length;
      const resolved = list.filter(c => c.status === 'RESOLVED' || c.status === 'CLOSED').length;

      // Render stats
      const statsEl = document.getElementById('citizen-stats');
      if (statsEl) {
        statsEl.innerHTML = `
          ${Complaints.renderStatCard('📋', 'Total Complaints', total, 'blue')}
          ${Complaints.renderStatCard('⏳', 'Pending', pending, 'yellow')}
          ${Complaints.renderStatCard('🔄', 'In Progress', inProgress, 'cyan')}
          ${Complaints.renderStatCard('✅', 'Resolved', resolved, 'green')}
        `;
      }

      // Render recent complaints
      const recentEl = document.getElementById('recent-complaints');
      if (recentEl) {
        if (list.length === 0) {
          recentEl.innerHTML = `
            <div class="empty-state">
              <div class="empty-icon">📝</div>
              <h3>No complaints yet</h3>
              <p>Report your first civic issue to get started.</p>
              <a href="report-issue.html" class="btn btn-primary mt-4">Report an Issue</a>
            </div>
          `;
        } else {
          // Show last 5
          const recent = list.slice(0, 5);
          recentEl.innerHTML = recent.map(c => Complaints.renderComplaintCard(c)).join('');
        }
      }
    } catch (err) {
      App.Toast.error(err.message);
    }
  }

  // ── Load Citizen Complaints List ───────────────────────
  async function loadComplaintsList(filters = {}) {
    const listEl = document.getElementById('complaints-list');
    if (!listEl) return;

    listEl.innerHTML = '<div class="loading-state"><div class="spinner lg"></div><p>Loading complaints...</p></div>';

    try {
      const complaints = await Complaints.getComplaints(filters);
      const list = Array.isArray(complaints) ? complaints : (complaints.content || []);

      if (list.length === 0) {
        listEl.innerHTML = `
          <div class="empty-state">
            <div class="empty-icon">🔍</div>
            <h3>No complaints found</h3>
            <p>No complaints match your current filters.</p>
          </div>
        `;
        return;
      }

      listEl.innerHTML = list.map(c => Complaints.renderComplaintCard(c)).join('');
    } catch (err) {
      listEl.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">⚠️</div>
          <h3>Error loading complaints</h3>
          <p>${App.escapeHtml(err.message)}</p>
        </div>
      `;
    }
  }

  // ── Load Complaint Detail ──────────────────────────────
  async function loadComplaintDetail(id) {
    try {
      const complaint = await Complaints.getComplaint(id);
      return complaint;
    } catch (err) {
      App.Toast.error(err.message);
      return null;
    }
  }

  // ── Load Officer Dashboard ─────────────────────────────
  async function loadOfficerDashboard() {
    try {
      const complaints = await Complaints.getOfficerComplaints();
      const list = Array.isArray(complaints) ? complaints : (complaints.content || []);

      const total = list.length;
      const pending = list.filter(c => c.status === 'PENDING' || c.status === 'ASSIGNED').length;
      const inProgress = list.filter(c => c.status === 'IN_PROGRESS').length;
      const resolved = list.filter(c => c.status === 'RESOLVED' || c.status === 'CLOSED').length;
      const critical = list.filter(c => c.priority === 'CRITICAL').length;

      const statsEl = document.getElementById('officer-stats');
      if (statsEl) {
        statsEl.innerHTML = `
          ${Complaints.renderStatCard('📋', 'Assigned', total, 'blue')}
          ${Complaints.renderStatCard('⏳', 'Pending', pending, 'yellow')}
          ${Complaints.renderStatCard('🔄', 'In Progress', inProgress, 'cyan')}
          ${Complaints.renderStatCard('✅', 'Resolved', resolved, 'green')}
          ${Complaints.renderStatCard('🚨', 'Critical', critical, 'red')}
        `;
      }

      // Render table
      renderOfficerTable(list);

      // Render map if container exists
      if (document.getElementById('officer-map')) {
        MapModule.initMap('officer-map');
        MapModule.addComplaintMarkers(list);
      }
    } catch (err) {
      App.Toast.error(err.message);
    }
  }

  function renderOfficerTable(complaints) {
    const tableBody = document.getElementById('officer-table-body');
    if (!tableBody) return;

    if (complaints.length === 0) {
      tableBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted" style="padding:2rem;">No complaints assigned.</td></tr>';
      return;
    }

    tableBody.innerHTML = complaints.map(c => `
      <tr>
        <td><strong>#${c.complaintId || c.id}</strong></td>
        <td>${App.categoryLabel(c.category)}</td>
        <td><span class="badge ${App.priorityBadge(c.priority)}">${c.priority}</span></td>
        <td>${App.formatDate(c.createdAt)}</td>
        <td><span class="badge ${App.statusBadge(c.status)}">${App.statusLabel(c.status)}</span></td>
        <td>
          <a href="officer-complaint-details.html?id=${c.id}" class="btn btn-sm btn-primary">View</a>
        </td>
      </tr>
    `).join('');
  }

  // ── Load Admin Dashboard ───────────────────────────────
  async function loadAdminDashboard() {
    try {
      const analytics = await Complaints.getAnalytics();

      // Stats
      const statsEl = document.getElementById('admin-stats');
      if (statsEl) {
        statsEl.innerHTML = `
          ${Complaints.renderStatCard('📋', 'Total Complaints', analytics.totalComplaints || 0, 'blue')}
          ${Complaints.renderStatCard('⏳', 'Pending', analytics.pending || 0, 'yellow')}
          ${Complaints.renderStatCard('📌', 'Assigned', analytics.assigned || 0, 'cyan')}
          ${Complaints.renderStatCard('🔄', 'In Progress', analytics.inProgress || 0, 'cyan')}
          ${Complaints.renderStatCard('✅', 'Resolved', analytics.resolved || 0, 'green')}
          ${Complaints.renderStatCard('🚨', 'Critical', analytics.critical || 0, 'red')}
          ${Complaints.renderStatCard('👥', 'Total Users', analytics.totalUsers || 0, 'blue')}
          ${Complaints.renderStatCard('👮', 'Officers', analytics.totalOfficers || 0, 'blue')}
        `;
      }

      // Category chart
      const catChartEl = document.getElementById('category-chart');
      if (catChartEl && analytics.byCategory) {
        const items = Object.entries(analytics.byCategory).map(([key, val]) => ({
          label: App.categoryLabel(key),
          value: val,
          color: 'blue',
        }));
        const max = Math.max(...items.map(i => i.value), 1);
        catChartEl.innerHTML = Complaints.renderBarChart(items, max);
      }

      // Status chart
      const statusChartEl = document.getElementById('status-chart');
      if (statusChartEl && analytics.byStatus) {
        const colors = { PENDING: 'yellow', ASSIGNED: 'cyan', IN_PROGRESS: 'blue', RESOLVED: 'green', CLOSED: 'gray' };
        const items = Object.entries(analytics.byStatus).map(([key, val]) => ({
          label: App.statusLabel(key),
          value: val,
          color: colors[key] || 'blue',
        }));
        const max = Math.max(...items.map(i => i.value), 1);
        statusChartEl.innerHTML = Complaints.renderBarChart(items, max);
      }

      // Department chart
      const deptChartEl = document.getElementById('department-chart');
      if (deptChartEl && analytics.byDepartment) {
        const items = Object.entries(analytics.byDepartment).map(([key, val]) => ({
          label: key,
          value: val,
          color: 'cyan',
        }));
        const max = Math.max(...items.map(i => i.value), 1);
        deptChartEl.innerHTML = Complaints.renderBarChart(items, max);
      }

      // Map
      if (document.getElementById('admin-map') && analytics.complaints) {
        MapModule.initMap('admin-map');
        MapModule.addComplaintMarkers(analytics.complaints);
      }

    } catch (err) {
      App.Toast.error(err.message);
    }
  }

  // ── Notifications ──────────────────────────────────────
  async function loadNotifications() {
    const listEl = document.getElementById('notifications-list');
    if (!listEl) return;

    try {
      const notifications = await Complaints.getNotifications();
      const list = Array.isArray(notifications) ? notifications : [];

      if (list.length === 0) {
        listEl.innerHTML = `
          <div class="empty-state">
            <div class="empty-icon">🔔</div>
            <h3>No notifications</h3>
            <p>You're all caught up!</p>
          </div>
        `;
        return;
      }

      listEl.innerHTML = list.map(n => `
        <div class="notification-item ${n.read ? '' : 'unread'}" data-id="${n.id}" onclick="Dashboard.handleNotificationClick(${n.id})">
          <div class="notif-dot"></div>
          <div class="notif-text">
            ${App.escapeHtml(n.message)}
            <div class="notif-time">${App.timeAgo(n.createdAt)}</div>
          </div>
        </div>
      `).join('');
    } catch (err) {
      App.Toast.error(err.message);
    }
  }

  async function handleNotificationClick(id) {
    try {
      await Complaints.markNotificationRead(id);
      const item = document.querySelector(`.notification-item[data-id="${id}"]`);
      if (item) item.classList.remove('unread');
    } catch (err) {
      // Silently ignore
    }
  }

  // ── Filter Form Handler ────────────────────────────────
  function initFilters(onFilter) {
    const filterForm = document.getElementById('filter-form');
    if (!filterForm) return;

    // Collect filter values and call callback
    const applyFilters = () => {
      const filters = {};
      filterForm.querySelectorAll('[data-filter]').forEach(el => {
        const key = el.dataset.filter;
        const val = el.value.trim();
        if (val) filters[key] = val;
      });
      onFilter(filters);
    };

    // Listen to changes
    filterForm.querySelectorAll('select[data-filter]').forEach(el => {
      el.addEventListener('change', applyFilters);
    });

    // Search input with debounce
    const searchInput = filterForm.querySelector('input[data-filter="search"]');
    if (searchInput) {
      let debounceTimer;
      searchInput.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(applyFilters, 400);
      });
    }

    // Reset button
    const resetBtn = filterForm.querySelector('[data-action="reset-filters"]');
    if (resetBtn) {
      resetBtn.addEventListener('click', () => {
        filterForm.querySelectorAll('[data-filter]').forEach(el => { el.value = ''; });
        applyFilters();
      });
    }
  }

  return {
    loadCitizenDashboard,
    loadComplaintsList,
    loadComplaintDetail,
    loadOfficerDashboard,
    loadAdminDashboard,
    loadNotifications,
    handleNotificationClick,
    initFilters,
  };
})();

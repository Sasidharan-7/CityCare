/**
 * CityCare - Core Application Module
 * 
 * Handles: API communication, toast notifications, sidebar, 
 * session management, and shared utilities.
 */

const App = (() => {
  // ── Configuration ──────────────────────────────────────
  const CONFIG = {
    API_BASE: window.location.origin.replace(/\/$/, '') + '/api',
    AI_SERVICE_URL: window.location.origin.replace(/\/$/, '').replace(/:\d+$/, ':5000'),
    TOKEN_KEY: 'citycare_token',
    USER_KEY: 'citycare_user',
    MAX_FILE_SIZE: 5 * 1024 * 1024, // 5 MB
    ALLOWED_IMAGE_TYPES: ['image/jpeg', 'image/png', 'image/webp'],
  };

  // ── Token / Session helpers ────────────────────────────
  function getToken() {
    return localStorage.getItem(CONFIG.TOKEN_KEY);
  }

  function setToken(token) {
    localStorage.setItem(CONFIG.TOKEN_KEY, token);
  }

  function getUser() {
    const raw = localStorage.getItem(CONFIG.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  function setUser(user) {
    localStorage.setItem(CONFIG.USER_KEY, JSON.stringify(user));
  }

  function clearSession() {
    localStorage.removeItem(CONFIG.TOKEN_KEY);
    localStorage.removeItem(CONFIG.USER_KEY);
  }

  function isLoggedIn() {
    return !!getToken();
  }

  /**
   * Redirect if user is not authenticated or lacks the required role.
   * Call at the top of every protected page's script.
   */
  function requireAuth(allowedRoles = []) {
    if (!isLoggedIn()) {
      window.location.href = 'login.html';
      return false;
    }
    if (allowedRoles.length > 0) {
      const user = getUser();
      if (!user || !allowedRoles.includes(user.role)) {
        window.location.href = 'login.html';
        return false;
      }
    }
    return true;
  }

  // ── API Fetch Wrapper ──────────────────────────────────
  /**
   * Makes an authenticated API call.
   * @param {string} endpoint - Path after API_BASE (e.g. '/complaints')
   * @param {object} options - fetch options override
   * @returns {Promise<object>} parsed JSON response
   */
  async function apiCall(endpoint, options = {}) {
    const url = CONFIG.API_BASE + endpoint;
    const headers = options.headers || {};

    // Attach auth token
    const token = getToken();
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    // Default content type for JSON bodies
    if (options.body && !(options.body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
      if (typeof options.body === 'object') {
        options.body = JSON.stringify(options.body);
      }
    }

    try {
      const response = await fetch(url, { ...options, headers });

      // Handle 401 - session expired
      if (response.status === 401) {
        clearSession();
        Toast.error('Session expired. Please login again.');
        setTimeout(() => { window.location.href = 'login.html'; }, 1500);
        throw new Error('Unauthorized');
      }

      // Parse response
      const contentType = response.headers.get('content-type');
      let data;
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        data = await response.text();
      }

      if (!response.ok) {
        const errMsg = (typeof data === 'object' && data.message) ? data.message : 'Request failed';
        throw new Error(errMsg);
      }

      return data;
    } catch (err) {
      if (err.message === 'Failed to fetch') {
        throw new Error('Unable to connect to CityCare server. Please ensure the backend is running.');
      }
      throw err;
    }
  }

  // ── Toast Notifications ────────────────────────────────
  const Toast = (() => {
    let container;

    function init() {
      container = document.getElementById('toast-container');
      if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container';
        document.body.appendChild(container);
      }
    }

    function show(message, type = 'info', duration = 4000) {
      if (!container) init();

      const toast = document.createElement('div');
      toast.className = `toast ${type}`;
      toast.innerHTML = `
        <span class="toast-message">${escapeHtml(message)}</span>
        <button class="toast-close" onclick="this.parentElement.remove()">&times;</button>
      `;
      container.appendChild(toast);

      setTimeout(() => {
        if (toast.parentElement) toast.remove();
      }, duration);
    }

    return {
      success: (msg) => show(msg, 'success'),
      error: (msg) => show(msg, 'error'),
      warning: (msg) => show(msg, 'warning'),
      info: (msg) => show(msg, 'info'),
    };
  })();

  // ── Sidebar Toggle (mobile) ────────────────────────────
  function initSidebar() {
    const sidebar = document.querySelector('.sidebar');
    const overlay = document.querySelector('.sidebar-overlay');
    const hamburger = document.getElementById('hamburger-btn');

    if (!sidebar) return;

    if (hamburger) {
      hamburger.addEventListener('click', () => {
        sidebar.classList.toggle('open');
        if (overlay) overlay.classList.toggle('active');
      });
    }

    if (overlay) {
      overlay.addEventListener('click', () => {
        sidebar.classList.remove('open');
        overlay.classList.remove('active');
      });
    }

    // Highlight active nav link
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    document.querySelectorAll('.sidebar-nav a').forEach((link) => {
      const linkPage = link.getAttribute('href');
      if (linkPage === currentPage) {
        link.classList.add('active');
      }
    });

    // Populate user info in sidebar footer
    const user = getUser();
    if (user) {
      const nameEl = document.querySelector('.sidebar-user .user-name');
      const roleEl = document.querySelector('.sidebar-user .user-role');
      const avatarEl = document.querySelector('.sidebar-user .user-avatar');
      if (nameEl) nameEl.textContent = user.name || 'User';
      if (roleEl) roleEl.textContent = capitalize(user.role || 'citizen');
      if (avatarEl) avatarEl.textContent = (user.name || 'U').charAt(0).toUpperCase();
    }
  }

  // ── Utility Functions ──────────────────────────────────

  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  function formatDateTime(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  function timeAgo(dateStr) {
    const now = new Date();
    const date = new Date(dateStr);
    const diffMs = now - date;
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1) return 'Just now';
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffHr = Math.floor(diffMin / 60);
    if (diffHr < 24) return `${diffHr}h ago`;
    const diffDay = Math.floor(diffHr / 24);
    if (diffDay < 30) return `${diffDay}d ago`;
    return formatDate(dateStr);
  }

  /**
   * Returns the appropriate CSS badge class for a status.
   */
  function statusBadge(status) {
    const map = {
      PENDING: 'badge-pending',
      ASSIGNED: 'badge-assigned',
      IN_PROGRESS: 'badge-inprogress',
      RESOLVED: 'badge-resolved',
      CLOSED: 'badge-closed',
    };
    return map[status] || 'badge-pending';
  }

  function priorityBadge(priority) {
    const map = {
      CRITICAL: 'badge-critical',
      HIGH: 'badge-high',
      MEDIUM: 'badge-medium',
      LOW: 'badge-low',
    };
    return map[priority] || 'badge-medium';
  }

  function statusLabel(status) {
    const map = {
      PENDING: 'Pending',
      ASSIGNED: 'Assigned',
      IN_PROGRESS: 'In Progress',
      RESOLVED: 'Resolved',
      CLOSED: 'Closed',
    };
    return map[status] || status;
  }

  function categoryLabel(cat) {
    const map = {
      POTHOLE: 'Pothole',
      GARBAGE: 'Garbage',
      WATER_LEAKAGE: 'Water Leakage',
      BROKEN_STREETLIGHT: 'Broken Streetlight',
      OPEN_DRAIN: 'Open Drain / Manhole',
      ROAD_DAMAGE: 'Road Damage',
      OTHER: 'Other',
    };
    return map[cat] || cat;
  }

  /**
   * Validate an image file (type + size).
   * Returns error string or null if valid.
   */
  function validateImage(file) {
    if (!file) return 'Please select an image.';
    if (!CONFIG.ALLOWED_IMAGE_TYPES.includes(file.type)) {
      return 'Please upload a JPEG, PNG, or WebP image.';
    }
    if (file.size > CONFIG.MAX_FILE_SIZE) {
      return 'Please upload an image smaller than 5 MB.';
    }
    return null;
  }

  // ── Logout ─────────────────────────────────────────────
  function logout() {
    clearSession();
    window.location.href = 'login.html';
  }

  // ── Init ───────────────────────────────────────────────
  function init() {
    initSidebar();

    // Attach logout buttons
    document.querySelectorAll('[data-action="logout"]').forEach((btn) => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        logout();
      });
    });
  }

  // Auto-init when DOM is ready
  document.addEventListener('DOMContentLoaded', init);

  // ── Public API ─────────────────────────────────────────
  return {
    CONFIG,
    getToken,
    setToken,
    getUser,
    setUser,
    clearSession,
    isLoggedIn,
    requireAuth,
    apiCall,
    Toast,
    escapeHtml,
    capitalize,
    formatDate,
    formatDateTime,
    timeAgo,
    statusBadge,
    priorityBadge,
    statusLabel,
    categoryLabel,
    validateImage,
    logout,
  };
})();

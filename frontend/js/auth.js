/**
 * CityCare - Authentication Module
 * 
 * Handles: Login, Registration, form validation
 */

const Auth = (() => {

  /**
   * Validate email format
   */
  function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  /**
   * Validate phone (10-digit Indian format)
   */
  function isValidPhone(phone) {
    return /^[6-9]\d{9}$/.test(phone);
  }

  /**
   * Validate password strength (min 6 chars)
   */
  function isValidPassword(password) {
    return password.length >= 6;
  }

  /**
   * Show field-level error
   */
  function showFieldError(fieldId, message) {
    const field = document.getElementById(fieldId);
    const error = document.getElementById(fieldId + '-error');
    if (field) field.classList.add('error');
    if (error) {
      error.textContent = message;
      error.classList.add('visible');
    }
  }

  /**
   * Clear field-level error
   */
  function clearFieldError(fieldId) {
    const field = document.getElementById(fieldId);
    const error = document.getElementById(fieldId + '-error');
    if (field) field.classList.remove('error');
    if (error) error.classList.remove('visible');
  }

  /**
   * Clear all errors in a form
   */
  function clearAllErrors(formId) {
    const form = document.getElementById(formId);
    if (!form) return;
    form.querySelectorAll('.form-control').forEach(f => f.classList.remove('error'));
    form.querySelectorAll('.form-error').forEach(e => e.classList.remove('visible'));
  }

  // ── Login ──────────────────────────────────────────────
  async function handleLogin(e) {
    e.preventDefault();
    clearAllErrors('login-form');

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    // Validate
    let valid = true;
    if (!email) { showFieldError('email', 'Email is required.'); valid = false; }
    else if (!isValidEmail(email)) { showFieldError('email', 'Enter a valid email address.'); valid = false; }
    if (!password) { showFieldError('password', 'Password is required.'); valid = false; }

    if (!valid) return;

    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<div class="spinner"></div> Signing in...';

    try {
      const data = await App.apiCall('/auth/login', {
        method: 'POST',
        body: { email, password },
      });

      // Store token and user info
      App.setToken(data.token);
      App.setUser({
        id: data.userId,
        name: data.name,
        email: data.email,
        role: data.role,
      });

      App.Toast.success('Welcome back, ' + data.name + '!');

      // Redirect based on role
      setTimeout(() => {
        switch (data.role) {
          case 'ADMIN':
            window.location.href = 'admin-dashboard.html';
            break;
          case 'OFFICER':
            window.location.href = 'officer-dashboard.html';
            break;
          default:
            window.location.href = 'citizen-dashboard.html';
            break;
        }
      }, 500);

    } catch (err) {
      App.Toast.error(err.message || 'Invalid email or password.');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Sign In';
    }
  }

  // ── Register ───────────────────────────────────────────
  async function handleRegister(e) {
    e.preventDefault();
    clearAllErrors('register-form');

    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    // Validate
    let valid = true;
    if (!name || name.length < 2) { showFieldError('name', 'Full name is required (at least 2 characters).'); valid = false; }
    if (!email) { showFieldError('email', 'Email is required.'); valid = false; }
    else if (!isValidEmail(email)) { showFieldError('email', 'Enter a valid email address.'); valid = false; }
    if (!phone) { showFieldError('phone', 'Phone number is required.'); valid = false; }
    else if (!isValidPhone(phone)) { showFieldError('phone', 'Enter a valid 10-digit phone number.'); valid = false; }
    if (!password) { showFieldError('password', 'Password is required.'); valid = false; }
    else if (!isValidPassword(password)) { showFieldError('password', 'Password must be at least 6 characters.'); valid = false; }
    if (password !== confirmPassword) { showFieldError('confirm-password', 'Passwords do not match.'); valid = false; }

    if (!valid) return;

    const submitBtn = e.target.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<div class="spinner"></div> Creating account...';

    try {
      const data = await App.apiCall('/auth/register', {
        method: 'POST',
        body: { name, email, phone, password },
      });

      App.Toast.success('Account created successfully! Please sign in.');
      setTimeout(() => { window.location.href = 'login.html'; }, 1500);

    } catch (err) {
      App.Toast.error(err.message || 'Registration failed. Please try again.');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Create Account';
    }
  }

  // ── Init ───────────────────────────────────────────────
  function init() {
    const loginForm = document.getElementById('login-form');
    if (loginForm) loginForm.addEventListener('submit', handleLogin);

    const registerForm = document.getElementById('register-form');
    if (registerForm) registerForm.addEventListener('submit', handleRegister);

    // Real-time error clearing
    document.querySelectorAll('.form-control').forEach((field) => {
      field.addEventListener('input', () => clearFieldError(field.id));
    });
  }

  document.addEventListener('DOMContentLoaded', init);

  return { handleLogin, handleRegister };
})();

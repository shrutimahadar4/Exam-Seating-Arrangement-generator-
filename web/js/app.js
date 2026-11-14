/**
 * Shared Utilities & API Client for ExamSeat Pro
 */

const API_BASE = '/api';

// Toast Notification System
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    
    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '❌';
    if (type === 'warning') icon = '⚠️';

    toast.innerHTML = `<span>${icon}</span> <span>${message}</span>`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.transition = 'all 0.3s ease';
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Session Storage Helpers
function setSession(token, user) {
    localStorage.setItem('auth_token', token);
    localStorage.setItem('auth_user', JSON.stringify(user));
}

function getToken() {
    return localStorage.getItem('auth_token');
}

function getUser() {
    const raw = localStorage.getItem('auth_user');
    try {
        return raw ? JSON.parse(raw) : null;
    } catch (e) {
        return null;
    }
}

function clearSession() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
}

async function logout() {
    try {
        await fetchApi('/auth/logout', 'POST');
    } catch (e) {
        // ignore
    }
    clearSession();
    window.location.href = '/index.html';
}

// Unified API Fetcher
async function fetchApi(endpoint, method = 'GET', data = null) {
    const headers = {
        'Content-Type': 'application/json'
    };

    const token = getToken();
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method,
        headers
    };

    if (data && (method === 'POST' || method === 'PUT')) {
        options.body = JSON.stringify(data);
    }

    const response = await fetch(API_BASE + endpoint, options);
    const contentType = response.headers.get('content-type');
    
    let result = null;
    if (contentType && contentType.includes('application/json')) {
        result = await response.json();
    } else {
        result = await response.text();
    }

    if (!response.ok) {
        const errorMsg = (result && result.message) ? result.message : `HTTP ${response.status}: Request failed`;
        throw new Error(errorMsg);
    }

    return result;
}

// Division badge class helper
function getDivisionBadgeHtml(divisionName) {
    const div = (divisionName || '').toUpperCase();
    let badgeClass = 'div-badge-A';
    if (div === 'B') badgeClass = 'div-badge-B';
    if (div === 'C') badgeClass = 'div-badge-C';
    if (div === 'D') badgeClass = 'div-badge-D';
    return `<span class="div-badge ${badgeClass}">Div ${div}</span>`;
}

// Academic Year badge helper
function getYearBadgeHtml(year) {
    const yr = (year || 'First Year');
    let cls = 'badge-year-fe';
    if (yr.includes('Second')) cls = 'badge-year-se';
    else if (yr.includes('Third')) cls = 'badge-year-te';
    else if (yr.includes('Fourth') || yr.includes('Final')) cls = 'badge-year-be';
    return `<span class="badge ${cls}">${yr}</span>`;
}

// Hold / Released status badge helper
function getHoldBadgeHtml(onHold) {
    if (onHold) {
        return `<span class="badge badge-hold">🔒 ON HOLD</span>`;
    }
    return `<span class="badge badge-released">🔓 RELEASED</span>`;
}

// Excel Download Trigger
function downloadExcelTemplate() {
    window.location.href = '/api/excel/template';
}

function downloadExcelSeating(examId) {
    const id = examId || 0;
    window.location.href = `/api/excel/export/seating?examId=${id}`;
}

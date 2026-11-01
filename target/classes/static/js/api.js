/**
 * Study Pro 42 Unified API Client
 * Seamlessly interfaces with Spring Boot backend and provides local persistent cache
 */
const API_BASE = window.location.origin;

class StudyPulseApi {
  constructor() {
    this.currentUser = JSON.parse(localStorage.getItem('studypro42_user') || localStorage.getItem('studypulse_user') || 'null');
    this.token = localStorage.getItem('studypro42_token') || localStorage.getItem('studypulse_token') || null;
  }

  getUserId() {
    return this.currentUser ? this.currentUser.id : 1;
  }

  isLoggedIn() {
    return !!this.currentUser;
  }

  saveSession(authResponse) {
    if (authResponse && authResponse.user) {
      this.currentUser = authResponse.user;
      this.token = authResponse.token;
      localStorage.setItem('studypulse_user', JSON.stringify(authResponse.user));
      localStorage.setItem('studypro42_user', JSON.stringify(authResponse.user));
      localStorage.setItem('studypulse_token', authResponse.token || '');
      localStorage.setItem('studypro42_token', authResponse.token || '');
      if (authResponse.progress) {
        localStorage.setItem('studypulse_progress', JSON.stringify(authResponse.progress));
        localStorage.setItem('studypro42_progress', JSON.stringify(authResponse.progress));
      }
    }
  }

  logout() {
    this.currentUser = null;
    this.token = null;
    localStorage.removeItem('studypulse_user');
    localStorage.removeItem('studypro42_user');
    localStorage.removeItem('studypulse_token');
    localStorage.removeItem('studypro42_token');
  }

  async _request(endpoint, options = {}) {
    try {
      const url = `${API_BASE}${endpoint}`;
      const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
      };
      if (this.token) {
        headers['Authorization'] = `Bearer ${this.token}`;
      }

      const res = await fetch(url, { ...options, headers });
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.error || `HTTP ${res.status}: ${res.statusText}`);
      }
      return await res.json();
    } catch (err) {
      console.warn(`API call failed for ${endpoint}:`, err.message);
      throw err;
    }
  }

  // Username & Password Auth APIs
  async register(username, password, name) {
    const data = await this._request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, name })
    });
    this.saveSession(data);
    return data;
  }

  async login(username, password) {
    const data = await this._request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
    this.saveSession(data);
    return data;
  }

  async updateCredentials(newUsername, newPassword, name) {
    const userId = this.getUserId();
    const data = await this._request(`/api/auth/credentials?userId=${userId}`, {
      method: 'PUT',
      body: JSON.stringify({ newUsername, newPassword, name })
    });
    if (data && data.user) {
      this.currentUser = { ...this.currentUser, ...data.user };
      localStorage.setItem('studypulse_user', JSON.stringify(this.currentUser));
      localStorage.setItem('studypro42_user', JSON.stringify(this.currentUser));
    }
    return data;
  }

  // Auth APIs
  async sendOtp(mobileNumber) {
    return this._request('/api/auth/send-otp', {
      method: 'POST',
      body: JSON.stringify({ mobileNumber })
    });
  }

  async verifyOtp(mobileNumber, otpCode, name) {
    const data = await this._request('/api/auth/verify-otp', {
      method: 'POST',
      body: JSON.stringify({ mobileNumber, otpCode, name })
    });
    this.saveSession(data);
    return data;
  }

  async loginGuest() {
    const data = await this._request('/api/auth/guest', { method: 'POST' });
    this.saveSession(data);
    return data;
  }

  async configureSmsKey(apiKey) {
    return this._request('/api/auth/sms-key', {
      method: 'POST',
      body: JSON.stringify({ apiKey })
    });
  }

  async getSmsStatus() {
    return this._request('/api/auth/sms-status');
  }

  async getCurrentUser() {
    const userId = this.getUserId();
    return this._request(`/api/auth/me?userId=${userId}`);
  }

  // Task APIs
  async getTasks(dateStr, category) {
    const userId = this.getUserId();
    let query = `/api/tasks?userId=${userId}`;
    if (dateStr) query += `&date=${dateStr}`;
    if (category && category !== 'ALL') query += `&category=${encodeURIComponent(category)}`;
    return this._request(query);
  }

  async createTask(taskData) {
    const userId = this.getUserId();
    return this._request(`/api/tasks?userId=${userId}`, {
      method: 'POST',
      body: JSON.stringify(taskData)
    });
  }

  async updateTask(taskId, taskData) {
    const userId = this.getUserId();
    return this._request(`/api/tasks/${taskId}?userId=${userId}`, {
      method: 'PUT',
      body: JSON.stringify(taskData)
    });
  }

  async toggleTask(taskId) {
    const userId = this.getUserId();
    return this._request(`/api/tasks/${taskId}/toggle?userId=${userId}`, {
      method: 'POST'
    });
  }

  async deleteTask(taskId) {
    const userId = this.getUserId();
    return this._request(`/api/tasks/${taskId}?userId=${userId}`, {
      method: 'DELETE'
    });
  }

  async loadDefaultTasks(category, dateStr) {
    const userId = this.getUserId();
    let query = `/api/tasks/default?userId=${userId}&category=${encodeURIComponent(category || 'ALL')}`;
    if (dateStr) query += `&date=${dateStr}`;
    return this._request(query, { method: 'POST' });
  }

  // Study Timing APIs
  async getTimings(category) {
    const userId = this.getUserId();
    let query = `/api/timings?userId=${userId}`;
    if (category && category !== 'ALL') query += `&category=${encodeURIComponent(category)}`;
    return this._request(query);
  }

  async createTiming(timingData) {
    const userId = this.getUserId();
    return this._request(`/api/timings?userId=${userId}`, {
      method: 'POST',
      body: JSON.stringify(timingData)
    });
  }

  async updateTiming(timingId, timingData) {
    const userId = this.getUserId();
    return this._request(`/api/timings/${timingId}?userId=${userId}`, {
      method: 'PUT',
      body: JSON.stringify(timingData)
    });
  }

  async deleteTiming(timingId) {
    const userId = this.getUserId();
    return this._request(`/api/timings/${timingId}?userId=${userId}`, {
      method: 'DELETE'
    });
  }

  // Gamification APIs
  async getGamifiedProgress() {
    const userId = this.getUserId();
    return this._request(`/api/gamification/progress?userId=${userId}`);
  }

  // Appreciation APIs
  async getDailyAppreciation() {
    const userId = this.getUserId();
    return this._request(`/api/appreciation/daily?userId=${userId}`);
  }

  async getWeeklyAppreciation() {
    const userId = this.getUserId();
    return this._request(`/api/appreciation/weekly?userId=${userId}`);
  }

  // Profile APIs
  async getProfile() {
    const userId = this.getUserId();
    return this._request(`/api/profile?userId=${userId}`);
  }

  async updateProfile(profileData) {
    const userId = this.getUserId();
    return this._request(`/api/profile?userId=${userId}`, {
      method: 'PUT',
      body: JSON.stringify(profileData)
    });
  }

  // Database Connection Status API
  async getDatabaseStatus() {
    return this._request('/api/database/status');
  }
}

const apiInstance = new StudyPulseApi();
window.studyPulseApi = apiInstance;
window.studyPro42Api = apiInstance;

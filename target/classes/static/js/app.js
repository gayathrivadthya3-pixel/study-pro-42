/**
 * Study Pro 42 Main Application Engine
 * Manages Dashboard, Timings, Daily Tasks, Gamification, Appreciation & Profile
 */
document.addEventListener('DOMContentLoaded', () => {
  const App = {
    currentTab: 'dashboard',
    currentCategory: 'ALL',
    selectedDate: new Date().toISOString().split('T')[0],
    tasks: [],
    timings: [],
    progress: null,
    profile: null,
    databaseStatus: null,
    alarmCheckerInterval: null,
    pomodoroInterval: null,
    pomodoroSeconds: 25 * 60,
    pomodoroRunning: false,

    async init() {
      console.log('🚀 Initializing Study Pro 42 App...');
      this.bindEvents();
      this.checkAuth();
      await this.loadAllData();
      this.startAlarmChecker();
      this.checkAppreciationAlerts();

      // Ensure user sets or logs into username & password at starting
      if (!window.studyPulseApi.isLoggedIn()) {
        this.openAuthModal();
        this.switchAuthTab('register');
      }

      // Register PWA service worker with auto-update
      if ('serviceWorker' in navigator) {
        navigator.serviceWorker.register('/sw.js').then((reg) => {
          reg.update();
        }).catch((err) => {
          console.warn('SW registration warning:', err);
        });
      }

      // Handle native Mobile / Desktop PWA installation prompt
      window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        window.studyPulseInstallPrompt = e;
        const installBtn = document.getElementById('pwa-install-btn');
        if (installBtn) {
          installBtn.classList.remove('hidden');
          installBtn.onclick = async () => {
            if (window.studyPulseInstallPrompt) {
              window.studyPulseInstallPrompt.prompt();
              const { outcome } = await window.studyPulseInstallPrompt.userChoice;
              if (outcome === 'accepted') {
                this.showToast('🚀 Study Pro 42 App installed to your home screen!', 'success');
              }
              window.studyPulseInstallPrompt = null;
              installBtn.classList.add('hidden');
            }
          };
        }
      });
    },

    bindEvents() {
      // Tab switching
      document.querySelectorAll('[data-tab]').forEach((btn) => {
        btn.addEventListener('click', (e) => {
          e.preventDefault();
          const target = btn.getAttribute('data-tab');
          this.switchTab(target);
        });
      });

      // Category filters
      document.querySelectorAll('[data-category-filter]').forEach((btn) => {
        btn.addEventListener('click', (e) => {
          const cat = btn.getAttribute('data-category-filter');
          this.filterCategory(cat);
        });
      });

      // Auth modal trigger
      const loginBtn = document.getElementById('open-login-btn');
      if (loginBtn) loginBtn.addEventListener('click', () => this.openAuthModal());

      // Task actions
      const openAddTaskBtn = document.getElementById('open-add-task-btn');
      if (openAddTaskBtn) openAddTaskBtn.addEventListener('click', () => this.openTaskModal());

      const taskForm = document.getElementById('task-form');
      if (taskForm) taskForm.addEventListener('submit', (e) => this.handleSaveTask(e));

      const loadDefaultsBtn = document.getElementById('load-defaults-btn');
      if (loadDefaultsBtn) loadDefaultsBtn.addEventListener('click', () => this.handleLoadDefaultTasks());

      const defaultCategorySelect = document.getElementById('default-track-select');
      if (defaultCategorySelect) {
        defaultCategorySelect.addEventListener('change', (e) => {
          this.handleLoadDefaultTasks(e.target.value);
        });
      }

      // Timing actions
      const openAddTimingBtn = document.getElementById('open-add-timing-btn');
      if (openAddTimingBtn) openAddTimingBtn.addEventListener('click', () => this.openTimingModal());

      const timingForm = document.getElementById('timing-form');
      if (timingForm) timingForm.addEventListener('submit', (e) => this.handleSaveTiming(e));

      // Profile update form
      const profileForm = document.getElementById('profile-form');
      if (profileForm) profileForm.addEventListener('submit', (e) => this.handleSaveProfile(e));

      // Audio test & Notification Permission buttons
      const testSoundBtn = document.getElementById('test-alarm-sound-btn');
      if (testSoundBtn) testSoundBtn.addEventListener('click', () => {
        window.studyPulseAudio.playTaskAlarmSound();
        this.showToast('🔔 Alarm Chime Tested!', 'success');
      });

      const notifyPermBtn = document.getElementById('enable-notifications-btn');
      if (notifyPermBtn) notifyPermBtn.addEventListener('click', async () => {
        const granted = await window.studyPulseAudio.requestNotificationPermission();
        if (granted) {
          this.showToast('✅ Browser Notifications Enabled!', 'success');
        } else {
          this.showToast('⚠️ Notifications permission denied.', 'warning');
        }
      });

      // Date selector
      const dateSelector = document.getElementById('task-date-selector');
      if (dateSelector) {
        dateSelector.value = this.selectedDate;
        dateSelector.addEventListener('change', (e) => {
          this.selectedDate = e.target.value;
          this.loadTasks();
        });
      }

      // Listen for custom in-app alarm trigger
      window.addEventListener('studypulse:alarm', (e) => {
        const task = e.detail.task;
        this.showAlarmModal(task);
      });

      // Live AM/PM preview listeners for time inputs
      const bindTimeBadge = (inputId, textId) => {
        const inp = document.getElementById(inputId);
        const txt = document.getElementById(textId);
        if (inp && txt) {
          const update = () => {
            txt.textContent = this.formatTime12(inp.value) || '--:-- --';
          };
          inp.addEventListener('input', update);
          inp.addEventListener('change', update);
          update();
        }
      };
      bindTimeBadge('task-time-input', 'task-time-ampm-text');
      bindTimeBadge('timing-start-input', 'timing-start-ampm-text');
      bindTimeBadge('timing-end-input', 'timing-end-ampm-text');
    },

    checkAuth() {
      const user = window.studyPulseApi.currentUser;
      const userDisplay = document.getElementById('user-display-name');
      const userAvatar = document.getElementById('user-avatar-img');
      const authActionBtn = document.getElementById('open-login-btn');

      if (user) {
        const displayName = user.username ? `@${user.username}` : (user.name || 'Scholar');
        if (userDisplay) {
          userDisplay.textContent = displayName;
          userDisplay.classList.remove('hidden');
        }
        if (userAvatar) userAvatar.src = user.avatar || `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(user.username || displayName)}`;
        if (authActionBtn) {
          authActionBtn.textContent = 'Account';
          authActionBtn.onclick = () => this.switchTab('profile');
        }
      } else {
        if (userDisplay) {
          userDisplay.textContent = 'Guest Scholar';
        }
        if (authActionBtn) {
          authActionBtn.textContent = 'Sign In';
          authActionBtn.onclick = () => {
            this.openAuthModal();
            this.switchAuthTab('signin');
          };
        }
      }
    },

    switchTab(tabName) {
      this.currentTab = tabName;

      // Update Nav buttons styling
      document.querySelectorAll('[data-tab]').forEach((btn) => {
        const isCurrent = btn.getAttribute('data-tab') === tabName;
        if (isCurrent) {
          btn.classList.add('text-indigo-600', 'dark:text-indigo-400', 'font-bold');
          btn.classList.remove('text-slate-500', 'dark:text-slate-400');
        } else {
          btn.classList.remove('text-indigo-600', 'dark:text-indigo-400', 'font-bold');
          btn.classList.add('text-slate-500', 'dark:text-slate-400');
        }
      });

      // Show/Hide section panels
      const sections = ['dashboard', 'timings', 'tasks', 'gamify', 'profile'];
      sections.forEach((sec) => {
        const el = document.getElementById(`section-${sec}`);
        if (el) {
          if (sec === tabName) {
            el.classList.remove('hidden');
          } else {
            el.classList.add('hidden');
          }
        }
      });

      // Refresh data when navigating
      if (tabName === 'tasks') this.loadTasks();
      if (tabName === 'timings') this.loadTimings();
      if (tabName === 'gamify') this.loadGamification();
      if (tabName === 'profile') this.loadProfile();
      if (tabName === 'dashboard') this.loadAllData();

      window.scrollTo({ top: 0, behavior: 'smooth' });
    },

    filterCategory(category) {
      this.currentCategory = category;
      document.querySelectorAll('[data-category-filter]').forEach((btn) => {
        const match = btn.getAttribute('data-category-filter') === category;
        if (match) {
          btn.classList.add('bg-indigo-600', 'text-white', 'shadow-md');
          btn.classList.remove('bg-slate-100', 'dark:bg-slate-800', 'text-slate-700', 'dark:text-slate-300');
        } else {
          btn.classList.remove('bg-indigo-600', 'text-white', 'shadow-md');
          btn.classList.add('bg-slate-100', 'dark:bg-slate-800', 'text-slate-700', 'dark:text-slate-300');
        }
      });

      this.renderTasks();
      this.renderTimings();
    },

    async loadAllData() {
      await Promise.all([
        this.loadTasks(),
        this.loadTimings(),
        this.loadGamification(),
        this.loadDatabaseStatus()
      ]);
      this.renderDashboard();
    },

    async loadTasks() {
      try {
        const tasks = await window.studyPulseApi.getTasks(this.selectedDate, this.currentCategory);
        this.tasks = tasks || [];
      } catch (err) {
        console.warn('Backend unavailable, using local tasks:', err);
      }
      this.renderTasks();
      this.renderDashboardProgress();
    },

    async loadTimings() {
      try {
        const timings = await window.studyPulseApi.getTimings(this.currentCategory);
        this.timings = timings || [];
      } catch (err) {
        console.warn('Backend unavailable for timings:', err);
      }
      this.renderTimings();
      this.renderDashboardTimings();
    },

    async loadGamification() {
      try {
        const progress = await window.studyPulseApi.getGamifiedProgress();
        this.progress = progress;
        this.renderGamifiedView();
      } catch (err) {
        console.warn('Gamification error:', err);
      }
    },

    async loadDatabaseStatus() {
      try {
        const status = await window.studyPulseApi.getDatabaseStatus();
        this.databaseStatus = status;
        this.renderDatabaseIndicator();
      } catch (e) {
        this.databaseStatus = { isAivenCloud: false, summary: 'Local Storage Active' };
        this.renderDatabaseIndicator();
      }
    },

    renderDatabaseIndicator() {
      const dbBadge = document.getElementById('db-status-badge');
      const dbText = document.getElementById('db-status-text');
      const dbDetails = document.getElementById('db-details-box');

      const isAiven = this.databaseStatus && this.databaseStatus.isAivenCloud;
      const isMySQL = this.databaseStatus && (this.databaseStatus.isMySQL || (this.databaseStatus.productName && this.databaseStatus.productName.toLowerCase().includes('mysql')));
      
      if (dbBadge) {
        if (isAiven) {
          dbBadge.className = 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-300 shadow-sm';
          dbBadge.innerHTML = '<span class="w-2 h-2 rounded-full bg-emerald-500 mr-1.5 animate-pulse"></span> Aiven MySQL Cloud';
        } else if (isMySQL) {
          dbBadge.className = 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800 dark:bg-blue-950/60 dark:text-blue-300 border border-blue-300 shadow-sm';
          dbBadge.innerHTML = '<span class="w-2 h-2 rounded-full bg-blue-600 mr-1.5 animate-pulse"></span> MySQL Database';
        } else {
          dbBadge.className = 'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold bg-indigo-100 text-indigo-800 dark:bg-indigo-950/60 dark:text-indigo-300 border border-indigo-300';
          dbBadge.innerHTML = '<span class="w-2 h-2 rounded-full bg-indigo-500 mr-1.5"></span> SQL Database';
        }
      }

      if (dbText) {
        dbText.textContent = this.databaseStatus ? this.databaseStatus.summary : 'MySQL Database Active';
      }

      if (dbDetails && this.databaseStatus) {
        dbDetails.innerHTML = `
          <div class="text-xs space-y-1">
            <p><strong>Database Engine:</strong> ${this.databaseStatus.productName || 'MySQL Server'}</p>
            <p><strong>Version:</strong> ${this.databaseStatus.productVersion || '8.0'}</p>
            <p><strong>Status:</strong> ${this.databaseStatus.connected ? '✅ Connected & Healthy' : '❌ Disconnected'}</p>
            <p><strong>Host / Provider:</strong> ${this.databaseStatus.cloudProvider || 'MySQL (Port 3306)'}</p>
          </div>
        `;
      }
    },

    // -------------------------------------------------------------
    // RENDER DASHBOARD
    // -------------------------------------------------------------
    renderDashboard() {
      this.renderDashboardProgress();
      this.renderDashboardTimings();
      this.renderTodayScheduleOverview();
    },

    renderDashboardProgress() {
      const total = this.tasks.length;
      const completed = this.tasks.filter((t) => t.completed).length;
      const percent = total > 0 ? Math.round((completed / total) * 100) : 0;

      const ringCircle = document.getElementById('dash-circle-progress');
      const percentText = document.getElementById('dash-percent-text');
      const taskFraction = document.getElementById('dash-task-fraction');

      if (percentText) percentText.textContent = `${percent}%`;
      if (taskFraction) taskFraction.textContent = `${completed} / ${total} Tasks Done`;

      if (ringCircle) {
        const circumference = 2 * Math.PI * 45; // r=45
        const offset = circumference - (percent / 100) * circumference;
        ringCircle.style.strokeDashoffset = offset;
      }
    },

    renderDashboardTimings() {
      const container = document.getElementById('dashboard-timings-grid');
      if (!container) return;

      const categories = [
        { key: 'SEMESTER', title: 'Semester Exam', icon: 'fa-graduation-cap', color: 'blue' },
        { key: 'GATE', title: 'GATE Exams', icon: 'fa-brain', color: 'red' },
        { key: 'LANGUAGE', title: 'Other Activities', icon: 'fa-shapes', color: 'emerald' },
        { key: 'INTERVIEW', title: 'Interview Prep', icon: 'fa-briefcase', color: 'purple' }
      ];

      container.innerHTML = categories.map((cat) => {
        const catTimings = this.timings.filter((t) => t.category === cat.key);
        const count = catTimings.length;
        const firstTime = catTimings[0] ? `${this.formatTime12(catTimings[0].startTime)} - ${this.formatTime12(catTimings[0].endTime)}` : 'No active timing';

        return `
          <div class="glass-card p-4 rounded-2xl flex flex-col justify-between cursor-pointer border hover:border-indigo-400"
               onclick="window.studyPulseApp.openCategory('${cat.key}')">
            <div class="flex items-center justify-between mb-2">
              <div class="w-10 h-10 rounded-xl bg-${cat.color}-100 dark:bg-${cat.color}-950/60 text-${cat.color}-600 dark:text-${cat.color}-400 flex items-center justify-center text-lg">
                <i class="fa-solid ${cat.icon}"></i>
              </div>
              <span class="text-xs px-2 py-0.5 rounded-full font-semibold bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                ${count} session${count === 1 ? '' : 's'}
              </span>
            </div>
            <div>
              <h4 class="font-bold text-slate-800 dark:text-slate-100 text-sm">${cat.title}</h4>
              <p class="text-xs text-slate-500 dark:text-slate-400 mt-1 flex items-center gap-1">
                <i class="fa-regular fa-clock text-[10px]"></i> ${firstTime}
              </p>
            </div>
          </div>
        `;
      }).join('');
    },

    renderTodayScheduleOverview() {
      const listEl = document.getElementById('dash-urgent-tasks-list');
      if (!listEl) return;

      const upcoming = this.tasks.filter((t) => !t.completed).slice(0, 3);
      if (upcoming.length === 0) {
        listEl.innerHTML = `
          <div class="text-center py-6 text-slate-400">
            <i class="fa-regular fa-circle-check text-3xl text-emerald-500 mb-2"></i>
            <p class="text-sm font-medium">All planned tasks completed for today!</p>
            <button onclick="window.studyPulseApp.switchTab('tasks')" class="mt-2 text-xs text-indigo-600 dark:text-indigo-400 underline">Add or load more tasks</button>
          </div>
        `;
        return;
      }

      listEl.innerHTML = upcoming.map((t) => `
        <div class="flex items-center justify-between p-3 rounded-xl bg-white/70 dark:bg-slate-800/70 border border-slate-200 dark:border-slate-700">
          <div class="flex items-center gap-3">
            <button onclick="window.studyPulseApp.toggleTask(${t.id})" class="w-6 h-6 rounded-lg border-2 border-slate-300 dark:border-slate-600 flex items-center justify-center hover:border-emerald-500 text-transparent hover:text-emerald-500 transition">
              <i class="fa-solid fa-check text-xs"></i>
            </button>
            <div>
              <h5 class="text-xs font-bold text-slate-800 dark:text-slate-100">${this.escapeHtml(t.title)}</h5>
              <div class="flex items-center gap-2 mt-0.5 text-[11px] text-slate-500 dark:text-slate-400">
                <span class="font-semibold text-indigo-600 dark:text-indigo-400">${this.getCategoryName(t.category)}</span>
                <span>•</span>
                <span><i class="fa-regular fa-clock"></i> ${this.formatTime12(t.scheduledTime) || 'Flexible'}</span>
              </div>
            </div>
          </div>
          <span class="text-xs font-bold px-2 py-0.5 rounded-full ${t.priority === 'HIGH' ? 'bg-rose-100 text-rose-600' : 'bg-slate-100 text-slate-600'}">
            ${t.priority}
          </span>
        </div>
      `).join('');
    },

    openCategory(cat) {
      this.filterCategory(cat);
      this.switchTab('tasks');
    },

    // -------------------------------------------------------------
    // RENDER STUDY TIMINGS (Timetable for the 4 tracks)
    // -------------------------------------------------------------
    renderTimings() {
      const container = document.getElementById('timings-container');
      if (!container) return;

      const filtered = this.currentCategory === 'ALL'
        ? this.timings
        : this.timings.filter((t) => t.category === this.currentCategory);

      if (filtered.length === 0) {
        container.innerHTML = `
          <div class="text-center py-12 text-slate-400 glass-card rounded-2xl p-8">
            <i class="fa-regular fa-calendar-xmark text-4xl mb-3 text-slate-300"></i>
            <p class="text-base font-semibold">No study schedules found for ${this.getCategoryName(this.currentCategory)}.</p>
            <p class="text-xs mt-1">Add your study hours or restore the recommended default timings.</p>
            <button onclick="window.studyPulseApp.openTimingModal()" class="mt-4 px-4 py-2 bg-indigo-600 text-white rounded-xl text-xs font-semibold hover:bg-indigo-700 shadow-md">
              <i class="fa-solid fa-plus mr-1"></i> Add Study Timing
            </button>
          </div>
        `;
        return;
      }

      container.innerHTML = filtered.map((item) => {
        const badgeClass = `badge-${item.category.toLowerCase()}`;
        return `
          <div class="glass-card p-5 rounded-2xl border flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div class="flex items-start gap-4">
              <div class="w-12 h-12 rounded-xl flex items-center justify-center text-xl font-bold ${badgeClass}">
                <i class="fa-solid ${this.getCategoryIcon(item.category)}"></i>
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold px-2.5 py-0.5 rounded-full ${badgeClass}">
                    ${this.getCategoryName(item.category)}
                  </span>
                  ${item.hasAlarm ? '<span class="text-xs text-rose-500 font-semibold flex items-center gap-1"><i class="fa-solid fa-bell"></i> Alarm On</span>' : ''}
                </div>
                <h4 class="text-base font-bold text-slate-900 dark:text-slate-100 mt-1">${this.escapeHtml(item.title)}</h4>
                <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">${this.escapeHtml(item.subjectOrTopic || 'General Study Session')}</p>
                <div class="flex items-center gap-3 mt-2 text-xs font-medium text-slate-600 dark:text-slate-300">
                  <span class="flex items-center gap-1 text-indigo-600 dark:text-indigo-400 font-bold">
                    <i class="fa-regular fa-clock"></i> ${this.formatTime12(item.startTime)} - ${this.formatTime12(item.endTime)}
                  </span>
                  <span>•</span>
                  <span><i class="fa-regular fa-calendar"></i> ${item.daysOfWeek}</span>
                </div>
              </div>
            </div>
            <div class="flex items-center gap-2 self-end md:self-center">
              <button onclick="window.studyPulseApp.startStudyPomodoro('${this.escapeHtml(item.title)}')" class="px-3 py-1.5 rounded-xl bg-indigo-600/10 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-600 hover:text-white text-xs font-semibold transition">
                <i class="fa-solid fa-play mr-1"></i> Study Now
              </button>
              <button onclick="window.studyPulseApp.openEditTimingModal(${item.id})" class="p-2 text-slate-400 hover:text-indigo-600 transition">
                <i class="fa-regular fa-pen-to-square"></i>
              </button>
              <button onclick="window.studyPulseApp.deleteTiming(${item.id})" class="p-2 text-slate-400 hover:text-rose-600 transition">
                <i class="fa-regular fa-trash-can"></i>
              </button>
            </div>
          </div>
        `;
      }).join('');
    },

    // -------------------------------------------------------------
    // RENDER DAILY TO-DO TASKS (with edit and default options)
    // -------------------------------------------------------------
    renderTasks() {
      const container = document.getElementById('tasks-container');
      if (!container) return;

      const filtered = this.currentCategory === 'ALL'
        ? this.tasks
        : this.tasks.filter((t) => t.category === this.currentCategory);

      if (filtered.length === 0) {
        container.innerHTML = `
          <div class="text-center py-12 glass-card rounded-2xl p-8 border">
            <i class="fa-regular fa-clipboard text-4xl mb-3 text-slate-300"></i>
            <h4 class="text-base font-semibold text-slate-700 dark:text-slate-200">No tasks for ${this.selectedDate} (${this.getCategoryName(this.currentCategory)})</h4>
            <p class="text-xs text-slate-500 mt-1 max-w-sm mx-auto">Click "Load Default Tasks" to immediately populate your day with high-yield study goals, or create custom tasks!</p>
            <div class="mt-4 flex flex-wrap items-center justify-center gap-2">
              <button onclick="window.studyPulseApp.handleLoadDefaultTasks('${this.currentCategory}')" class="px-4 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-xl text-xs font-bold shadow-md hover:opacity-95">
                <i class="fa-solid fa-bolt mr-1"></i> Load ${this.currentCategory === 'ALL' ? 'Master' : this.getCategoryName(this.currentCategory)} Defaults
              </button>
              <button onclick="window.studyPulseApp.openTaskModal()" class="px-4 py-2 bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 rounded-xl text-xs font-semibold hover:bg-slate-200">
                <i class="fa-solid fa-plus mr-1"></i> Add Custom Task
              </button>
            </div>
          </div>
        `;
        return;
      }

      container.innerHTML = filtered.map((task) => {
        const badgeClass = `badge-${task.category.toLowerCase()}`;
        const isDone = task.completed;
        const priorityColors = {
          HIGH: 'bg-rose-100 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300 border-rose-300',
          MEDIUM: 'bg-amber-100 text-amber-700 dark:bg-amber-950/60 dark:text-amber-300 border-amber-300',
          LOW: 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-300 border-emerald-300'
        };

        return `
          <div class="glass-card p-4 rounded-2xl border transition ${isDone ? 'opacity-65 bg-slate-50/50 dark:bg-slate-900/50' : ''} flex items-start justify-between gap-3 relative group">
            <div class="flex items-start gap-3 flex-1">
              <!-- Animated Checkbox -->
              <button onclick="window.studyPulseApp.toggleTask(${task.id}, event)"
                      class="mt-1 w-6 h-6 rounded-lg border-2 flex items-center justify-center transition-all ${isDone ? 'bg-emerald-500 border-emerald-500 text-white shadow-sm' : 'border-slate-300 dark:border-slate-600 text-transparent hover:border-indigo-500'}">
                <i class="fa-solid fa-check text-xs"></i>
              </button>

              <div class="flex-1">
                <div class="flex flex-wrap items-center gap-2 mb-1">
                  <span class="text-[11px] font-bold px-2 py-0.5 rounded-full ${badgeClass}">
                    ${this.getCategoryName(task.category)}
                  </span>
                  <span class="text-[10px] font-bold px-2 py-0.5 rounded-full border ${priorityColors[task.priority] || priorityColors.MEDIUM}">
                    ${task.priority}
                  </span>
                  <span class="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400">
                    +${task.xpValue || 30} XP
                  </span>
                  ${task.hasAlarm ? '<span class="text-[11px] text-rose-500 flex items-center gap-1 font-semibold"><i class="fa-solid fa-bell"></i> Alarm</span>' : ''}
                </div>

                <h4 class="text-sm md:text-base font-bold text-slate-900 dark:text-slate-100 ${isDone ? 'line-through text-slate-400 dark:text-slate-500' : ''}">
                  ${this.escapeHtml(task.title)}
                </h4>

                ${task.description ? `<p class="text-xs text-slate-500 dark:text-slate-400 mt-1">${this.escapeHtml(task.description)}</p>` : ''}

                <div class="flex items-center gap-3 mt-2 text-xs text-slate-500 dark:text-slate-400">
                  <span class="flex items-center gap-1 font-semibold text-slate-700 dark:text-slate-300">
                    <i class="fa-regular fa-clock"></i> ${this.formatTime12(task.scheduledTime) || 'Anytime'}
                  </span>
                  <span>•</span>
                  <span>${task.durationMinutes || 45} mins</span>
                </div>
              </div>
            </div>

            <!-- Action buttons (Edit & Delete) -->
            <div class="flex items-center gap-1">
              <button onclick="window.studyPulseApp.openEditTaskModal(${task.id})" title="Edit Task"
                      class="p-2 text-slate-400 hover:text-indigo-600 dark:hover:text-indigo-400 transition">
                <i class="fa-regular fa-pen-to-square"></i>
              </button>
              <button onclick="window.studyPulseApp.deleteTask(${task.id})" title="Delete Task"
                      class="p-2 text-slate-400 hover:text-rose-600 transition">
                <i class="fa-regular fa-trash-can"></i>
              </button>
            </div>
          </div>
        `;
      }).join('');
    },

    // -------------------------------------------------------------
    // TASK OPERATIONS (Toggle, Create, Edit, Delete, Default Tasks)
    // -------------------------------------------------------------
    async toggleTask(taskId, event) {
      try {
        const updated = await window.studyPulseApi.toggleTask(taskId);
        if (updated.completed) {
          window.studyPulseAudio.playSuccessSound();
          if (event) this.showFloatingXp(event, `+${updated.xpValue || 30} XP`);
          this.triggerConfettiSmall();
        }

        await Promise.all([this.loadTasks(), this.loadGamification()]);
        this.checkAppreciationAlerts();
      } catch (err) {
        this.showToast('Failed to update task: ' + err.message, 'error');
      }
    },

    openTaskModal(task = null) {
      const modal = document.getElementById('task-modal');
      const titleEl = document.getElementById('task-modal-title');
      const idInput = document.getElementById('task-id-input');

      if (!modal) return;

      if (task) {
        titleEl.textContent = 'Edit Study Task';
        idInput.value = task.id;
        document.getElementById('task-title-input').value = task.title;
        document.getElementById('task-desc-input').value = task.description || '';
        document.getElementById('task-cat-input').value = task.category;
        document.getElementById('task-priority-input').value = task.priority;
        document.getElementById('task-time-input').value = task.scheduledTime || '09:00';
        document.getElementById('task-duration-input').value = task.durationMinutes || 45;
        document.getElementById('task-alarm-input').checked = !!task.hasAlarm;
      } else {
        titleEl.textContent = 'Add New Study Task';
        idInput.value = '';
        document.getElementById('task-title-input').value = '';
        document.getElementById('task-desc-input').value = '';
        document.getElementById('task-cat-input').value = this.currentCategory === 'ALL' ? 'SEMESTER' : this.currentCategory;
        document.getElementById('task-priority-input').value = 'MEDIUM';
        document.getElementById('task-time-input').value = '09:00';
        document.getElementById('task-duration-input').value = 45;
        document.getElementById('task-alarm-input').checked = true;
      }

      const timeInput = document.getElementById('task-time-input');
      if (timeInput) timeInput.dispatchEvent(new Event('input'));

      modal.classList.remove('hidden');
    },

    openEditTaskModal(taskId) {
      const task = this.tasks.find((t) => t.id === taskId);
      if (task) this.openTaskModal(task);
    },

    closeTaskModal() {
      const modal = document.getElementById('task-modal');
      if (modal) modal.classList.add('hidden');
    },

    async handleSaveTask(e) {
      e.preventDefault();
      const id = document.getElementById('task-id-input').value;
      const taskData = {
        title: document.getElementById('task-title-input').value.trim(),
        description: document.getElementById('task-desc-input').value.trim(),
        category: document.getElementById('task-cat-input').value,
        priority: document.getElementById('task-priority-input').value,
        scheduledDate: this.selectedDate,
        scheduledTime: document.getElementById('task-time-input').value,
        durationMinutes: parseInt(document.getElementById('task-duration-input').value, 10) || 45,
        hasAlarm: document.getElementById('task-alarm-input').checked
      };

      if (!taskData.title) {
        this.showToast('Please enter a task title', 'warning');
        return;
      }

      try {
        if (id) {
          await window.studyPulseApi.updateTask(id, taskData);
          this.showToast('Task updated successfully!', 'success');
        } else {
          await window.studyPulseApi.createTask(taskData);
          this.showToast('New study task added!', 'success');
        }
        this.closeTaskModal();
        await this.loadTasks();
      } catch (err) {
        this.showToast('Failed to save task: ' + err.message, 'error');
      }
    },

    async deleteTask(taskId) {
      if (!confirm('Are you sure you want to delete this task?')) return;
      try {
        await window.studyPulseApi.deleteTask(taskId);
        this.showToast('Task deleted', 'info');
        await this.loadTasks();
      } catch (err) {
        this.showToast('Failed to delete task: ' + err.message, 'error');
      }
    },

    async handleLoadDefaultTasks(category = null) {
      const targetCategory = category || this.currentCategory;
      try {
        await window.studyPulseApi.loadDefaultTasks(targetCategory, this.selectedDate);
        this.showToast(`Loaded default tasks for ${targetCategory === 'ALL' ? 'all study tracks' : targetCategory}!`, 'success');
        this.triggerConfettiSmall();
        await this.loadTasks();
      } catch (err) {
        this.showToast('Failed to load default tasks: ' + err.message, 'error');
      }
    },

    // -------------------------------------------------------------
    // STUDY TIMINGS CRUD & POMODORO
    // -------------------------------------------------------------
    openTimingModal(timing = null) {
      const modal = document.getElementById('timing-modal');
      const titleEl = document.getElementById('timing-modal-title');
      const idInput = document.getElementById('timing-id-input');

      if (!modal) return;

      if (timing) {
        titleEl.textContent = 'Edit Study Timing';
        idInput.value = timing.id;
        document.getElementById('timing-title-input').value = timing.title;
        document.getElementById('timing-topic-input').value = timing.subjectOrTopic || '';
        document.getElementById('timing-cat-input').value = timing.category;
        document.getElementById('timing-start-input').value = timing.startTime;
        document.getElementById('timing-end-input').value = timing.endTime;
        document.getElementById('timing-days-input').value = timing.daysOfWeek || 'MON,TUE,WED,THU,FRI,SAT,SUN';
        document.getElementById('timing-alarm-input').checked = !!timing.hasAlarm;
      } else {
        titleEl.textContent = 'Create Study Timing';
        idInput.value = '';
        document.getElementById('timing-title-input').value = '';
        document.getElementById('timing-topic-input').value = '';
        document.getElementById('timing-cat-input').value = this.currentCategory === 'ALL' ? 'GATE' : this.currentCategory;
        document.getElementById('timing-start-input').value = '09:00';
        document.getElementById('timing-end-input').value = '11:30';
        document.getElementById('timing-days-input').value = 'MON,TUE,WED,THU,FRI,SAT';
        document.getElementById('timing-alarm-input').checked = true;
      }

      const sInp = document.getElementById('timing-start-input');
      const eInp = document.getElementById('timing-end-input');
      if (sInp) sInp.dispatchEvent(new Event('input'));
      if (eInp) eInp.dispatchEvent(new Event('input'));

      modal.classList.remove('hidden');
    },

    openEditTimingModal(timingId) {
      const timing = this.timings.find((t) => t.id === timingId);
      if (timing) this.openTimingModal(timing);
    },

    closeTimingModal() {
      const modal = document.getElementById('timing-modal');
      if (modal) modal.classList.add('hidden');
    },

    async handleSaveTiming(e) {
      e.preventDefault();
      const id = document.getElementById('timing-id-input').value;
      const timingData = {
        title: document.getElementById('timing-title-input').value.trim(),
        subjectOrTopic: document.getElementById('timing-topic-input').value.trim(),
        category: document.getElementById('timing-cat-input').value,
        startTime: document.getElementById('timing-start-input').value,
        endTime: document.getElementById('timing-end-input').value,
        daysOfWeek: document.getElementById('timing-days-input').value,
        hasAlarm: document.getElementById('timing-alarm-input').checked
      };

      if (!timingData.title || !timingData.startTime || !timingData.endTime) {
        this.showToast('Please fill in title and timings', 'warning');
        return;
      }

      try {
        if (id) {
          await window.studyPulseApi.updateTiming(id, timingData);
          this.showToast('Study timing updated!', 'success');
        } else {
          await window.studyPulseApi.createTiming(timingData);
          this.showToast('New study schedule created!', 'success');
        }
        this.closeTimingModal();
        await this.loadTimings();
      } catch (err) {
        this.showToast('Failed to save study timing: ' + err.message, 'error');
      }
    },

    async deleteTiming(timingId) {
      if (!confirm('Are you sure you want to delete this study schedule?')) return;
      try {
        await window.studyPulseApi.deleteTiming(timingId);
        this.showToast('Schedule deleted', 'info');
        await this.loadTimings();
      } catch (err) {
        this.showToast('Failed to delete timing: ' + err.message, 'error');
      }
    },

    startStudyPomodoro(title) {
      this.switchTab('dashboard');
      const timerSection = document.getElementById('pomodoro-timer-card');
      if (timerSection) {
        timerSection.scrollIntoView({ behavior: 'smooth' });
        const label = document.getElementById('pomodoro-task-label');
        if (label) label.textContent = title;
      }
      this.resetPomodoro(25 * 60);
      this.togglePomodoro();
    },

    togglePomodoro() {
      const btn = document.getElementById('pomodoro-toggle-btn');
      if (this.pomodoroRunning) {
        clearInterval(this.pomodoroInterval);
        this.pomodoroRunning = false;
        if (btn) btn.innerHTML = '<i class="fa-solid fa-play mr-1"></i> Resume';
      } else {
        this.pomodoroRunning = true;
        if (btn) btn.innerHTML = '<i class="fa-solid fa-pause mr-1"></i> Pause';
        this.pomodoroInterval = setInterval(() => {
          if (this.pomodoroSeconds > 0) {
            this.pomodoroSeconds--;
            this.renderPomodoroTimer();
          } else {
            clearInterval(this.pomodoroInterval);
            this.pomodoroRunning = false;
            window.studyPulseAudio.playTaskAlarmSound();
            alert('🎉 Pomodoro Study Session Complete! Take a 5-minute breather.');
          }
        }, 1000);
      }
    },

    resetPomodoro(seconds = 25 * 60) {
      clearInterval(this.pomodoroInterval);
      this.pomodoroRunning = false;
      this.pomodoroSeconds = seconds;
      const btn = document.getElementById('pomodoro-toggle-btn');
      if (btn) btn.innerHTML = '<i class="fa-solid fa-play mr-1"></i> Start Session';
      this.renderPomodoroTimer();
    },

    renderPomodoroTimer() {
      const mins = Math.floor(this.pomodoroSeconds / 60);
      const secs = this.pomodoroSeconds % 60;
      const display = `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
      const el = document.getElementById('pomodoro-timer-display');
      if (el) el.textContent = display;
    },

    // -------------------------------------------------------------
    // GAMIFICATION VIEW & BADGES
    // -------------------------------------------------------------
    renderGamifiedView() {
      if (!this.progress) return;

      const p = this.progress;
      const percent = Math.min(100, Math.max(0, Math.round(p.progressPercent || 0)));

      // Level & Title
      const levelNum = document.getElementById('gamify-level-num');
      const levelTitle = document.getElementById('gamify-level-title');
      const totalXp = document.getElementById('gamify-total-xp');
      const nextXp = document.getElementById('gamify-next-xp');
      const barFill = document.getElementById('gamify-xp-bar-fill');
      const streakCount = document.getElementById('gamify-streak-count');
      const tasksCompleted = document.getElementById('gamify-tasks-completed');
      const percentBadge = document.getElementById('gamify-progress-percent-badge');
      const percentPill = document.getElementById('gamify-percent-pill');
      const percentText = document.getElementById('gamify-percent-text');
      const innerPercent = document.getElementById('gamify-bar-inner-percent');

      if (levelNum) levelNum.textContent = `LEVEL ${p.currentLevel}`;
      if (levelTitle) levelTitle.textContent = p.levelTitle;
      if (totalXp) totalXp.textContent = `${p.totalXp} XP`;
      if (nextXp) nextXp.textContent = `${p.nextLevelXp} XP`;
      if (barFill) barFill.style.width = `${percent}%`;
      if (streakCount) streakCount.textContent = `${p.currentStreak} Day${p.currentStreak === 1 ? '' : 's'}`;
      if (tasksCompleted) tasksCompleted.textContent = p.totalTasksCompleted;
      if (percentBadge) percentBadge.textContent = `${percent}%`;
      if (percentPill) percentPill.textContent = `${percent}%`;
      if (percentText) percentText.textContent = `${percent}%`;
      if (innerPercent) {
        innerPercent.textContent = `${percent}%`;
        innerPercent.style.display = percent >= 12 ? 'inline' : 'none';
      }

      // Badges
      this.renderBadges(p.badges || []);
    },

    renderBadges(unlockedList) {
      const container = document.getElementById('badges-collection-grid');
      if (!container) return;

      const allBadges = [
        { id: 'FIRST_STEP', title: 'First Step', desc: 'Completed your first study task', icon: 'fa-shoe-prints', color: 'blue' },
        { id: 'STREAK_3', title: '3-Day Blaze', desc: 'Sustained a 3-day study streak', icon: 'fa-fire-flame-curved', color: 'amber' },
        { id: 'STREAK_7', title: '7-Day Titan', desc: 'Conquered a full 7-day study streak', icon: 'fa-fire', color: 'rose' },
        { id: 'CENTURION', title: 'Centurion', desc: 'Amassed over 1,000 XP in focused work', icon: 'fa-shield-halved', color: 'emerald' },
        { id: 'GATE_CONQUEROR', title: 'GATE Crusher', desc: 'Conquered advanced Engineering PYQs', icon: 'fa-brain', color: 'red' },
        { id: 'POLYGLOT_STAR', title: 'Activity Master', desc: 'Consistent other activities & skills practice', icon: 'fa-shapes', color: 'teal' },
        { id: 'CODE_GLADIATOR', title: 'Interview Gladiator', desc: 'Mastered LeetCode and System Design', icon: 'fa-code', color: 'purple' },
        { id: 'DAILY_STAR', title: 'Daily Star', desc: 'Finished >= 80% daily goals', icon: 'fa-star', color: 'yellow' },
        { id: 'WEEKLY_CHAMPION', title: 'Weekly Champion', desc: 'Consistently conquered weekly curriculum', icon: 'fa-crown', color: 'amber' }
      ];

      container.innerHTML = allBadges.map((badge) => {
        const isUnlocked = unlockedList.includes(badge.id);
        return `
          <div class="glass-card p-4 rounded-2xl flex flex-col items-center text-center border transition ${isUnlocked ? 'border-amber-400 bg-amber-50/20 dark:bg-amber-950/20' : 'opacity-40 grayscale'}">
            <div class="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl mb-2 ${isUnlocked ? `bg-${badge.color}-100 text-${badge.color}-600 dark:bg-${badge.color}-950/60 shadow-lg` : 'bg-slate-200 text-slate-400 dark:bg-slate-800'}">
              <i class="fa-solid ${badge.icon}"></i>
            </div>
            <h5 class="text-xs font-bold text-slate-800 dark:text-slate-100">${badge.title}</h5>
            <p class="text-[10px] text-slate-500 dark:text-slate-400 mt-1">${badge.desc}</p>
            <span class="mt-2 text-[10px] font-bold px-2 py-0.5 rounded-full ${isUnlocked ? 'bg-amber-100 text-amber-800 dark:bg-amber-900/60 dark:text-amber-300' : 'bg-slate-100 text-slate-400 dark:bg-slate-800'}">
              ${isUnlocked ? '✨ Unlocked' : '🔒 Locked'}
            </span>
          </div>
        `;
      }).join('');
    },

    // -------------------------------------------------------------
    // APPRECIATION SYSTEM (Daily and Weekly Celebrations)
    // -------------------------------------------------------------
    async checkAppreciationAlerts() {
      try {
        const daily = await window.studyPulseApi.getDailyAppreciation();
        const banner = document.getElementById('daily-appreciation-banner');

        if (daily && daily.qualified) {
          if (banner) {
            banner.classList.remove('hidden');
            const headlineEl = document.getElementById('appreciation-banner-headline');
            const msgEl = document.getElementById('appreciation-banner-msg');
            if (headlineEl) headlineEl.textContent = daily.headline;
            if (msgEl) msgEl.textContent = daily.message;
          }
        } else {
          if (banner) banner.classList.add('hidden');
        }
      } catch (e) {
        console.warn('Appreciation check failed:', e);
      }
    },

    async openAppreciationModal(type = 'DAILY') {
      try {
        const report = type === 'WEEKLY'
          ? await window.studyPulseApi.getWeeklyAppreciation()
          : await window.studyPulseApi.getDailyAppreciation();

        const modal = document.getElementById('appreciation-modal');
        if (!modal) return;

        document.getElementById('celebration-title').textContent = report.headline || 'Remarkable Effort!';
        document.getElementById('celebration-message').textContent = report.message || 'You are making steady progress toward your targets!';
        document.getElementById('celebration-quote').textContent = report.motivationalQuote || '"Consistency is the DNA of mastery."';
        document.getElementById('celebration-stats').textContent = `${report.tasksCompleted} / ${report.totalTasks} Tasks (${report.completionPercentage}%)`;
        document.getElementById('celebration-xp-bonus').textContent = `+${report.bonusXp || 50} XP Awarded!`;

        modal.classList.remove('hidden');
        window.studyPulseAudio.playLevelUpSound();
        this.triggerConfettiGrand();
      } catch (err) {
        this.showToast('Could not load appreciation details: ' + err.message, 'error');
      }
    },

    closeAppreciationModal() {
      const modal = document.getElementById('appreciation-modal');
      if (modal) modal.classList.add('hidden');
    },

    // -------------------------------------------------------------
    // ALARM MONITORING ENGINE
    // -------------------------------------------------------------
    startAlarmChecker() {
      if (this.alarmCheckerInterval) clearInterval(this.alarmCheckerInterval);
      this.alarmCheckerInterval = setInterval(() => {
        window.studyPulseAudio.checkTaskAlarms(this.tasks);
      }, 10000); // Check every 10 seconds
    },

    showAlarmModal(task) {
      const modal = document.getElementById('alarm-trigger-modal');
      if (!modal) return;

      document.getElementById('alarm-modal-task-title').textContent = task.title;
      document.getElementById('alarm-modal-task-cat').textContent = `${task.category} • Scheduled at ${this.formatTime12(task.scheduledTime)}`;
      document.getElementById('alarm-modal-task-desc').textContent = task.description || 'Important study goal scheduled for now!';

      const doneBtn = document.getElementById('alarm-mark-done-btn');
      if (doneBtn) {
        doneBtn.onclick = () => {
          this.toggleTask(task.id);
          this.closeAlarmModal();
        };
      }

      modal.classList.remove('hidden');
    },

    closeAlarmModal() {
      const modal = document.getElementById('alarm-trigger-modal');
      if (modal) modal.classList.add('hidden');
    },

    // -------------------------------------------------------------
    // PROFILE TAB
    // -------------------------------------------------------------
    // -------------------------------------------------------------
    // PROFILE TAB: USERNAME, USER PROGRESS & TASKS COMPLETED DETAILS
    // -------------------------------------------------------------
    async loadProfile() {
      try {
        const user = await window.studyPulseApi.getProfile();
        this.profile = user;
        const progress = await window.studyPulseApi.getGamifiedProgress();
        this.progress = progress;

        // 1. Populate Username Display & Credentials
        if (user) {
          const rawUsername = user.username || user.name || 'Scholar';
          const usernameDisplay = document.getElementById('profile-username-main');
          if (usernameDisplay) usernameDisplay.textContent = user.username ? `@${user.username}` : `@${rawUsername}`;

          const usernameInput = document.getElementById('profile-username-input');
          if (usernameInput) usernameInput.value = user.username || '';

          const avatarLarge = document.getElementById('profile-avatar-large');
          if (avatarLarge) {
            avatarLarge.src = user.avatar || `https://api.dicebear.com/7.x/bottts/svg?seed=${encodeURIComponent(user.username || rawUsername)}`;
          }
        }

        // 2. Populate User Progress Details
        if (progress) {
          const levelBadge = document.getElementById('profile-level-badge');
          if (levelBadge) levelBadge.textContent = `Level ${progress.currentLevel}`;

          const levelTitle = document.getElementById('profile-level-title');
          if (levelTitle) levelTitle.textContent = progress.levelTitle || 'Novice Scholar';

          const progressPct = document.getElementById('profile-progress-percentage');
          if (progressPct) progressPct.textContent = `${progress.progressPercent}%`;

          const progressBar = document.getElementById('profile-progress-bar');
          if (progressBar) progressBar.style.width = `${progress.progressPercent}%`;

          const xpNeeded = document.getElementById('profile-xp-needed-text');
          if (xpNeeded) xpNeeded.textContent = `${progress.totalXp} / ${progress.nextLevelXp} XP`;

          const totalXp = document.getElementById('profile-total-xp');
          if (totalXp) totalXp.textContent = `${progress.totalXp} XP`;

          const streak = document.getElementById('profile-current-streak');
          if (streak) streak.textContent = `${progress.currentStreak} Day${progress.currentStreak !== 1 ? 's' : ''} 🔥`;

          const todayPct = document.getElementById('profile-today-percentage');
          if (todayPct) todayPct.textContent = `${progress.todayProgressPercent}%`;

          const studyMins = document.getElementById('profile-study-minutes');
          if (studyMins) studyMins.textContent = `${progress.totalStudyMinutes} Mins ⏱️`;

          // 3. Populate Tasks Completed Summary
          const tasksBadge = document.getElementById('profile-tasks-completed-badge');
          if (tasksBadge) tasksBadge.textContent = `${progress.totalTasksCompleted} Completed`;

          const allTimeTasks = document.getElementById('profile-alltime-tasks');
          if (allTimeTasks) allTimeTasks.textContent = `${progress.totalTasksCompleted} Tasks`;

          const todayRatio = document.getElementById('profile-today-tasks-ratio');
          if (todayRatio) todayRatio.textContent = `${progress.todayCompletedCount} / ${progress.todayTotalCount} Tasks`;
        }

        // Render completed tasks list into profile
        this.renderProfileCompletedTasks();

      } catch (err) {
        console.warn('Profile load failed:', err);
      }
    },

    renderProfileCompletedTasks() {
      const container = document.getElementById('profile-completed-tasks-list');
      const countText = document.getElementById('profile-completed-count-text');
      if (!container) return;

      const completedTasks = (this.tasks || []).filter(t => t.completed);

      if (countText) {
        countText.textContent = `${completedTasks.length} task${completedTasks.length !== 1 ? 's' : ''} completed today`;
      }

      if (completedTasks.length === 0) {
        container.innerHTML = `
          <div class="p-6 rounded-2xl bg-slate-50 dark:bg-slate-800/40 text-center text-xs text-slate-400 border border-dashed border-slate-200 dark:border-slate-800">
            <i class="fa-regular fa-clipboard-check text-2xl mb-1.5 text-slate-300 dark:text-slate-600 block"></i>
            No tasks marked completed yet today.<br>Conquer tasks on Dashboard to see them logged here!
          </div>
        `;
        return;
      }

      const catBadges = {
        SEMESTER: { name: 'Semester', color: 'indigo', icon: 'fa-graduation-cap' },
        GATE: { name: 'GATE', color: 'rose', icon: 'fa-brain' },
        LANGUAGE: { name: 'Other Activities', color: 'teal', icon: 'fa-shapes' },
        INTERVIEW: { name: 'Interview', color: 'purple', icon: 'fa-code' },
        GENERAL: { name: 'General', color: 'blue', icon: 'fa-list-check' }
      };

      container.innerHTML = completedTasks.map(task => {
        const cat = catBadges[task.category] || catBadges.GENERAL;
        const timeFormatted = this.formatTime12(task.scheduledTime);
        return `
          <div class="p-3 rounded-2xl bg-emerald-50/50 dark:bg-emerald-950/20 border border-emerald-200/80 dark:border-emerald-800/50 flex items-center justify-between gap-3 animate-in fade-in duration-200">
            <div class="flex items-center gap-2.5 min-w-0">
              <i class="fa-solid fa-circle-check text-emerald-500 text-base shrink-0"></i>
              <div class="min-w-0">
                <h5 class="text-xs font-bold text-slate-800 dark:text-slate-200 line-through opacity-80 truncate">${this.escapeHtml(task.title)}</h5>
                <div class="flex items-center gap-2 text-[10px] text-slate-500 mt-0.5">
                  <span class="font-semibold text-${cat.color}-600 dark:text-${cat.color}-400 flex items-center gap-1">
                    <i class="fa-solid ${cat.icon}"></i> ${cat.name}
                  </span>
                  ${timeFormatted ? `<span>• ${timeFormatted}</span>` : ''}
                </div>
              </div>
            </div>
            <span class="shrink-0 px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
              +${task.xpReward || 30} XP
            </span>
          </div>
        `;
      }).join('');
    },

    async handleUpdateCredentials(e) {
      if (e && e.preventDefault) e.preventDefault();
      const userInp = document.getElementById('profile-username-input');
      const passInp = document.getElementById('profile-new-password-input');
      const confInp = document.getElementById('profile-confirm-password-input');

      const newUsername = userInp ? userInp.value.trim() : '';
      const newPassword = passInp ? passInp.value : '';
      const confirmPassword = confInp ? confInp.value : '';

      if (!newUsername || newUsername.length < 3) {
        this.showToast('Username must be at least 3 characters', 'warning');
        return;
      }

      if (newPassword) {
        if (newPassword.length < 3) {
          this.showToast('New password must be at least 3 characters', 'warning');
          return;
        }
        if (newPassword !== confirmPassword) {
          this.showToast('New passwords do not match!', 'error');
          return;
        }
      }

      try {
        const res = await window.studyPulseApi.updateCredentials(newUsername, newPassword || null);
        this.showToast('🔐 Account credentials updated successfully!', 'success');
        if (passInp) passInp.value = '';
        if (confInp) confInp.value = '';
        this.checkAuth();

        const usernameMain = document.getElementById('profile-username-main');
        if (usernameMain && res.user && res.user.username) {
          usernameMain.textContent = `@${res.user.username}`;
        }
      } catch (err) {
        this.showToast('Failed to update credentials: ' + err.message, 'error');
      }
    },

    // -------------------------------------------------------------
    // USERNAME & PASSWORD AUTHENTICATION ENGINE
    // -------------------------------------------------------------
    openAuthModal() {
      const modal = document.getElementById('auth-modal');
      if (modal) modal.classList.remove('hidden');
    },

    closeAuthModal() {
      const modal = document.getElementById('auth-modal');
      if (modal) modal.classList.add('hidden');
    },

    switchAuthTab(tab) {
      const formSignin = document.getElementById('form-signin');
      const formRegister = document.getElementById('form-register');
      const tabSignin = document.getElementById('auth-tab-signin');
      const tabRegister = document.getElementById('auth-tab-register');
      const headerTitle = document.getElementById('auth-modal-header-title');

      if (tab === 'register') {
        if (formSignin) formSignin.classList.add('hidden');
        if (formRegister) formRegister.classList.remove('hidden');
        if (tabRegister) {
          tabRegister.className = 'flex-1 py-2 text-xs font-bold rounded-xl transition bg-white dark:bg-slate-900 text-emerald-600 dark:text-emerald-400 shadow-sm';
        }
        if (tabSignin) {
          tabSignin.className = 'flex-1 py-2 text-xs font-bold rounded-xl transition text-slate-600 dark:text-slate-400 hover:text-slate-900';
        }
        if (headerTitle) {
          headerTitle.innerHTML = '<i class="fa-solid fa-user-plus text-emerald-600"></i> Create Scholar Account';
        }
      } else {
        if (formSignin) formSignin.classList.remove('hidden');
        if (formRegister) formRegister.classList.add('hidden');
        if (tabSignin) {
          tabSignin.className = 'flex-1 py-2 text-xs font-bold rounded-xl transition bg-white dark:bg-slate-900 text-indigo-600 dark:text-indigo-400 shadow-sm';
        }
        if (tabRegister) {
          tabRegister.className = 'flex-1 py-2 text-xs font-bold rounded-xl transition text-slate-600 dark:text-slate-400 hover:text-slate-900';
        }
        if (headerTitle) {
          headerTitle.innerHTML = '<i class="fa-solid fa-shield-halved text-indigo-600"></i> Study Pro 42 Sign In';
        }
      }
    },

    togglePasswordVisibility(inputId, btn) {
      const input = document.getElementById(inputId);
      if (!input) return;
      const icon = btn ? btn.querySelector('i') : null;
      if (input.type === 'password') {
        input.type = 'text';
        if (icon) {
          icon.classList.remove('fa-eye');
          icon.classList.add('fa-eye-slash');
        }
      } else {
        input.type = 'password';
        if (icon) {
          icon.classList.remove('fa-eye-slash');
          icon.classList.add('fa-eye');
        }
      }
    },

    async handleUsernameRegister(e) {
      if (e && e.preventDefault) e.preventDefault();
      const usernameInput = document.getElementById('reg-username');
      const passwordInput = document.getElementById('reg-password');
      const confirmInput = document.getElementById('reg-confirm-password');
      const nameInput = document.getElementById('reg-name');

      const username = usernameInput ? usernameInput.value.trim() : '';
      const password = passwordInput ? passwordInput.value : '';
      const confirmPassword = confirmInput ? confirmInput.value : '';
      const name = nameInput ? nameInput.value.trim() : '';

      if (!username || username.length < 3) {
        this.showToast('Username must be at least 3 characters', 'warning');
        return;
      }
      if (!password || password.length < 3) {
        this.showToast('Password must be at least 3 characters', 'warning');
        return;
      }
      if (password !== confirmPassword) {
        this.showToast('Passwords do not match!', 'error');
        return;
      }

      try {
        const res = await window.studyPulseApi.register(username, password, name);
        const displayName = res.user ? (res.user.username || res.user.name) : username;
        this.showToast(`🎉 Account created! Welcome, @${displayName}!`, 'success');
        this.closeAuthModal();
        this.checkAuth();
        await this.loadAllData();
      } catch (err) {
        this.showToast('Registration failed: ' + err.message, 'error');
      }
    },

    async handleUsernameLogin(e) {
      if (e && e.preventDefault) e.preventDefault();
      const usernameInput = document.getElementById('signin-username');
      const passwordInput = document.getElementById('signin-password');

      const username = usernameInput ? usernameInput.value.trim() : '';
      const password = passwordInput ? passwordInput.value : '';

      if (!username || !password) {
        this.showToast('Please enter both username and password', 'warning');
        return;
      }

      try {
        const res = await window.studyPulseApi.login(username, password);
        const displayName = res.user ? (res.user.username || res.user.name) : username;
        this.showToast(`👋 Welcome back, @${displayName}!`, 'success');
        this.closeAuthModal();
        this.checkAuth();
        await this.loadAllData();
      } catch (err) {
        this.showToast('Login failed: ' + err.message, 'error');
      }
    },

    async handleHardRefresh() {
      try {
        if ('serviceWorker' in navigator) {
          const registrations = await navigator.serviceWorker.getRegistrations();
          for (const reg of registrations) {
            await reg.unregister();
          }
        }
        if ('caches' in window) {
          const keys = await caches.keys();
          for (const k of keys) {
            await caches.delete(k);
          }
        }
      } catch (e) {
        console.warn('Cache purge error:', e);
      }
      window.location.href = window.location.pathname + '?nocache=' + Date.now();
    },

    handleLogout() {
      if (confirm('Are you sure you want to log out?')) {
        window.studyPulseApi.logout();
        this.showToast('Logged out.', 'info');
        this.checkAuth();
        this.switchTab('dashboard');
      }
    },

    // -------------------------------------------------------------
    // UTILITIES, EFFECTS & TOASTS
    // -------------------------------------------------------------
    showToast(message, type = 'info') {
      const container = document.getElementById('toast-container');
      if (!container) return;

      const toast = document.createElement('div');
      const colors = {
        success: 'bg-emerald-600 text-white',
        warning: 'bg-amber-600 text-white',
        error: 'bg-rose-600 text-white',
        info: 'bg-indigo-600 text-white'
      };

      toast.className = `px-4 py-3 rounded-2xl shadow-xl flex items-center gap-3 text-xs md:text-sm font-semibold transition-all transform duration-300 translate-y-4 opacity-0 ${colors[type] || colors.info}`;
      toast.innerHTML = `<span>${this.escapeHtml(message)}</span>`;

      container.appendChild(toast);
      requestAnimationFrame(() => {
        toast.classList.remove('translate-y-4', 'opacity-0');
      });

      setTimeout(() => {
        toast.classList.add('opacity-0', 'translate-y-2');
        setTimeout(() => toast.remove(), 300);
      }, 3500);
    },

    showFloatingXp(event, text) {
      const popup = document.createElement('div');
      popup.className = 'xp-popup text-base';
      popup.textContent = text;
      popup.style.left = `${event.pageX}px`;
      popup.style.top = `${event.pageY}px`;
      document.body.appendChild(popup);
      setTimeout(() => popup.remove(), 1200);
    },

    triggerConfettiSmall() {
      if (typeof confetti === 'function') {
        confetti({
          particleCount: 45,
          spread: 60,
          origin: { y: 0.8 }
        });
      }
    },

    triggerConfettiGrand() {
      if (typeof confetti === 'function') {
        confetti({
          particleCount: 150,
          spread: 100,
          origin: { y: 0.6 }
        });
      }
    },

    getCategoryIcon(cat) {
      switch (cat) {
        case 'SEMESTER': return 'fa-graduation-cap';
        case 'GATE': return 'fa-brain';
        case 'LANGUAGE': return 'fa-shapes';
        case 'INTERVIEW': return 'fa-briefcase';
        default: return 'fa-book-open';
      }
    },

    getCategoryName(cat) {
      switch (cat) {
        case 'SEMESTER': return 'Semester Exam';
        case 'GATE': return 'GATE Exams';
        case 'LANGUAGE': return 'Other Activities';
        case 'INTERVIEW': return 'Interview Prep';
        case 'GENERAL': return 'General To-Do';
        case 'ALL': return 'All Tracks';
        default: return cat || '';
      }
    },

    formatTime12(timeStr) {
      if (!timeStr) return '';
      const str = String(timeStr).trim();
      if (str.toUpperCase().includes('AM') || str.toUpperCase().includes('PM')) {
        return str;
      }
      const parts = str.split(':');
      if (parts.length < 2) return str;

      let hours = parseInt(parts[0], 10);
      const minutes = parts[1].slice(0, 2).padStart(2, '0');
      if (isNaN(hours)) return str;

      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours ? hours : 12;
      const formattedHours = String(hours).padStart(2, '0');

      return `${formattedHours}:${minutes} ${ampm}`;
    },

    toggleAmPm(inputId, targetAmPm) {
      const input = document.getElementById(inputId);
      if (!input || !input.value) return;
      const parts = input.value.split(':');
      if (parts.length < 2) return;
      let h = parseInt(parts[0], 10);
      const m = parts[1];
      if (isNaN(h)) return;

      if (targetAmPm === 'AM' && h >= 12) {
        h = h - 12;
      } else if (targetAmPm === 'PM' && h < 12) {
        h = h + 12;
      }
      input.value = `${String(h).padStart(2, '0')}:${m}`;
      input.dispatchEvent(new Event('input'));
    },

    escapeHtml(str) {
      if (!str) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }
  };

  window.studyPulseApp = App;
  window.studyPro42App = App;
  App.init();
});

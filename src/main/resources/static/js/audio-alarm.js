/**
 * StudyPulse Web Audio & Alarm Engine
 * Provides synthesized audio chimes, task reminder alarms, and Web Notifications
 */
class StudyPulseAudio {
  constructor() {
    this.audioCtx = null;
    this.soundEnabled = true;
    this.activeAlarmTimers = new Map();
    this.triggeredAlarms = new Set();
  }

  initContext() {
    if (!this.audioCtx) {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (AudioContext) {
        this.audioCtx = new AudioContext();
      }
    }
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume();
    }
  }

  /**
   * Play Task Completion Chime (Satisfying positive ding)
   */
  playSuccessSound() {
    if (!this.soundEnabled) return;
    this.initContext();
    if (!this.audioCtx) return;

    const ctx = this.audioCtx;
    const now = ctx.currentTime;

    const osc1 = ctx.createOscillator();
    const osc2 = ctx.createOscillator();
    const gainNode = ctx.createGain();

    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(523.25, now); // C5
    osc1.frequency.exponentialRampToValueAtTime(659.25, now + 0.1); // E5
    osc1.frequency.exponentialRampToValueAtTime(783.99, now + 0.2); // G5
    osc1.frequency.exponentialRampToValueAtTime(1046.50, now + 0.35); // C6

    osc2.type = 'triangle';
    osc2.frequency.setValueAtTime(523.25, now);
    osc2.frequency.exponentialRampToValueAtTime(1046.50, now + 0.35);

    gainNode.gain.setValueAtTime(0.01, now);
    gainNode.gain.linearRampToValueAtTime(0.3, now + 0.05);
    gainNode.gain.exponentialRampToValueAtTime(0.001, now + 0.8);

    osc1.connect(gainNode);
    osc2.connect(gainNode);
    gainNode.connect(ctx.destination);

    osc1.start(now);
    osc2.start(now);
    osc1.stop(now + 0.8);
    osc2.stop(now + 0.8);
  }

  /**
   * Play Incoming SMS / OTP Message Tone (Double Ding)
   */
  playMessageSmsChime() {
    if (!this.soundEnabled) return;
    this.initContext();
    if (!this.audioCtx) return;

    const ctx = this.audioCtx;
    const now = ctx.currentTime;

    const osc1 = ctx.createOscillator();
    const gain1 = ctx.createGain();
    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(880, now);
    gain1.gain.setValueAtTime(0.25, now);
    gain1.gain.exponentialRampToValueAtTime(0.001, now + 0.15);
    osc1.connect(gain1);
    gain1.connect(ctx.destination);
    osc1.start(now);
    osc1.stop(now + 0.15);

    const osc2 = ctx.createOscillator();
    const gain2 = ctx.createGain();
    osc2.type = 'sine';
    osc2.frequency.setValueAtTime(1318.5, now + 0.18);
    gain2.gain.setValueAtTime(0.3, now + 0.18);
    gain2.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
    osc2.connect(gain2);
    gain2.connect(ctx.destination);
    osc2.start(now + 0.18);
    osc2.stop(now + 0.45);
  }

  /**
   * Play Level Up Fanfare
   */
  playLevelUpSound() {
    if (!this.soundEnabled) return;
    this.initContext();
    if (!this.audioCtx) return;

    const ctx = this.audioCtx;
    const now = ctx.currentTime;
    const notes = [440, 554.37, 659.25, 880]; // A4, C#5, E5, A5

    notes.forEach((freq, idx) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      const startTime = now + idx * 0.12;
      const duration = 0.35;

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(freq, startTime);

      gain.gain.setValueAtTime(0.01, startTime);
      gain.gain.linearRampToValueAtTime(0.25, startTime + 0.04);
      gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(startTime);
      osc.stop(startTime + duration);
    });
  }

  /**
   * Play Important Task Alarm (Pulsing Urgent / Reminder chime)
   */
  playTaskAlarmSound() {
    if (!this.soundEnabled) return;
    this.initContext();
    if (!this.audioCtx) return;

    const ctx = this.audioCtx;
    const now = ctx.currentTime;

    // Beep 1
    this._createBeep(ctx, now, 880, 0.18);
    this._createBeep(ctx, now + 0.22, 1046.5, 0.18);
    // Beep 2
    this._createBeep(ctx, now + 0.55, 880, 0.18);
    this._createBeep(ctx, now + 0.77, 1046.5, 0.3);
  }

  _createBeep(ctx, startTime, freq, duration) {
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'sine';
    osc.frequency.setValueAtTime(freq, startTime);

    gain.gain.setValueAtTime(0.01, startTime);
    gain.gain.linearRampToValueAtTime(0.35, startTime + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start(startTime);
    osc.stop(startTime + duration);
  }

  /**
   * Request browser notifications permission
   */
  async requestNotificationPermission() {
    if (!('Notification' in window)) {
      console.warn('Notifications not supported by this browser');
      return false;
    }
    if (Notification.permission === 'granted') {
      return true;
    }
    const permission = await Notification.requestPermission();
    return permission === 'granted';
  }

  /**
   * Trigger an alarm notification for a task
   */
  triggerAlarm(task) {
    // 1. Play sound
    this.playTaskAlarmSound();

    // 2. Browser native notification if granted
    if ('Notification' in window && Notification.permission === 'granted') {
      try {
        new Notification(`⏰ Study Pro 42 Alarm: ${task.title}`, {
          body: `Category: ${task.category} | Time: ${task.scheduledTime || 'Now'} | ${task.description || 'Time to study!'}`,
          icon: 'https://api.dicebear.com/7.x/shapes/svg?seed=StudyPro42&backgroundColor=6366f1',
          vibrate: [200, 100, 200, 100, 300]
        });
      } catch (e) {
        console.warn('Native notification failed:', e);
      }
    }

    // 3. Dispatch custom in-app event for UI banner
    window.dispatchEvent(new CustomEvent('studypulse:alarm', { detail: { task } }));
  }

  /**
   * Monitor scheduled tasks for today and trigger alarms at task time
   */
  checkTaskAlarms(tasks) {
    if (!tasks || !Array.isArray(tasks)) return;

    const now = new Date();
    const currentHours = String(now.getHours()).padStart(2, '0');
    const currentMins = String(now.getMinutes()).padStart(2, '0');
    const currentTimeStr = `${currentHours}:${currentMins}`;
    const todayStr = now.toISOString().split('T')[0];

    tasks.forEach((task) => {
      if (task.hasAlarm && !task.completed && task.scheduledTime) {
        const taskKey = `${task.id}_${task.scheduledDate || todayStr}_${task.scheduledTime}`;
        if (task.scheduledTime === currentTimeStr && !this.triggeredAlarms.has(taskKey)) {
          this.triggeredAlarms.add(taskKey);
          this.triggerAlarm(task);
        }
      }
    });
  }
}

const audioInstance = new StudyPulseAudio();
window.studyPulseAudio = audioInstance;
window.studyPro42Audio = audioInstance;

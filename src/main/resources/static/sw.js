// Study Pro 42 PWA Service Worker v16 (Zero Cold-Start Screen & Instant Offline Shell)
const CACHE_NAME = 'studypro42-v16';
const PRECACHE_URLS = [
  '/',
  '/index.html',
  '/css/styles.css',
  '/js/app.js',
  '/js/api.js',
  '/manifest.json'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(PRECACHE_URLS).catch((err) => console.warn('Pre-cache warning:', err));
    })
  );
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            return caches.delete(key);
          }
        })
      );
    })
  );
  self.clients.claim();
});

self.addEventListener('fetch', (event) => {
  // Always bypass cache for API calls
  if (event.request.url.includes('/api/')) {
    return;
  }

  // Intercept navigation requests to prevent Render cold-start terminal screen from displaying
  event.respondWith(
    fetch(event.request)
      .then(async (networkResponse) => {
        if (networkResponse && networkResponse.status === 200) {
          if (event.request.mode === 'navigate' || event.request.destination === 'document') {
            const clone = networkResponse.clone();
            const text = await clone.text().catch(() => '');
            // If Render returned its cold-start wake up screen, serve the cached app instead
            if (text.includes('APPLICATION LOADING') || text.includes('WELCOME TO RENDER') || text.includes('SERVICE WAKING UP')) {
              const cached = await caches.match('/index.html');
              if (cached) return cached;
            }
          }
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, responseToCache));
        }
        return networkResponse;
      })
      .catch(() => caches.match(event.request).then((res) => res || caches.match('/index.html')))
  );
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      if (clientList.length > 0) {
        return clientList[0].focus();
      }
      return clients.openWindow('/');
    })
  );
});

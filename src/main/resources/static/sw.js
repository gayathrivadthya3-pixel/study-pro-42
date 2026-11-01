// Study Pro 42 PWA Service Worker v15 (Clean Empty Slate on Sign In & Custom Tasks Only)
const CACHE_NAME = 'studypro42-v15';

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => caches.delete(key)) // clear all old cached files immediately
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

  // Network-First strategy: fetch from server first, fallback to cache only if completely offline
  event.respondWith(
    fetch(event.request)
      .then((networkResponse) => {
        if (networkResponse && networkResponse.status === 200) {
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

// TICKET-ADV100 — theme toggle click handler.
// Initial theme + FOUC prevention is handled by the inline <script> in
// dashboard.html's <head>, which runs before the stylesheet loads; this
// file only wires the toggle button once the DOM is ready.
(function () {
  document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('theme-toggle');
    if (!btn) return;

    btn.setAttribute('aria-pressed', document.documentElement.dataset.theme === 'dark');

    btn.addEventListener('click', () => {
      const next = document.documentElement.dataset.theme === 'light' ? 'dark' : 'light';
      document.documentElement.dataset.theme = next;
      localStorage.setItem('reconx-theme', next);
      btn.setAttribute('aria-pressed', next === 'dark');
    });
  });
})();

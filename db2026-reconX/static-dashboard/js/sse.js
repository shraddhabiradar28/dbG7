// TICKET-ADV104 — EventSource subscription to /api/v1/trades/stream
// TICKET-ADV105 — prepend-and-animate with XSS-safe rendering + 50-entry DOM cap
(function () {
  'use strict';

  const FEED_EL   = document.getElementById('trade-feed');
  const STATUS_EL = document.getElementById('sse-status');
  if (!FEED_EL) return; // guard: script may load on pages without the feed

  const STREAM_URL = '/api/v1/trades/stream';
  let sse = null;

  // ── Helpers ────────────────────────────────────────────────────────────────
  function updateBadge(text) {
    if (STATUS_EL) STATUS_EL.textContent = text;
  }

  /** Always escape server-provided strings before inserting into innerHTML. */
  function escapeHtml(s) {
    return String(s)
      .replace(/&/g,  '&amp;')
      .replace(/</g,  '&lt;')
      .replace(/>/g,  '&gt;')
      .replace(/"/g,  '&quot;')
      .replace(/'/g,  '&#39;');
  }

  const fmtQty   = new Intl.NumberFormat('en-US');
  const fmtPrice = new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  });

  // ── TICKET-ADV105 — prepend one trade card ─────────────────────────────────
  function prependTradeRow(trade) {
    const statusMap = { MATCHED: 'matched', BREAK: 'break', UNMATCHED: 'break' };
    const mod = statusMap[trade.status] || 'pending';

    const el = document.createElement('article');
    // trade-card--new triggers the combined slide-in + fade-in entrance;
    // strip it after 500 ms once the CSS animation finishes.
    el.className = 'trade-card trade-card--' + mod + ' trade-card--new';
    
    // Wrapped in the correct header and body tags for the CSS
    el.innerHTML =
      '<header class="trade-card__header">' +
        '<strong>' + escapeHtml(trade.tradeRef) + '</strong> ' +
        '<span>[' + escapeHtml(trade.status) + ']</span>' +
      '</header>' +
      '<div class="trade-card__body">' +
        '<span>' + escapeHtml(trade.symbol) + '</span> ' +
        '<span>qty=' + fmtQty.format(trade.qty != null ? trade.qty : (trade.quantity || 0)) + '</span> ' +
        '<span>price=' + fmtPrice.format(trade.price || 0) + '</span> ' +
      '</div>';

    FEED_EL.prepend(el);

    // Remove the --new modifier once the 0.4 s CSS animation completes.
    setTimeout(function () { el.classList.remove('trade-card--new'); }, 500);

    // Cap the feed at 50 entries so the DOM stays bounded after a long session.
    while (FEED_EL.children.length > 50) {
      FEED_EL.lastElementChild.remove();
    }
  }

  // ── TICKET-ADV104 — EventSource connection ─────────────────────────────────
  function connect() {
    sse = new EventSource(STREAM_URL);

    sse.onopen = function () {
      updateBadge('Live');
    };

    sse.onmessage = function (e) {
      try {
        prependTradeRow(JSON.parse(e.data));
      } catch (_) {
        // malformed JSON from server — ignore silently
      }
    };

    // IMPORTANT: do NOT call connect() again inside onerror.
    // EventSource auto-reconnects with exponential backoff. Calling connect()
    // here would flood the dev server with cascading connection attempts.
    sse.onerror = function () {
      updateBadge('Reconnecting…');
    };
  }

  // Clean up when the user navigates away.
  window.addEventListener('beforeunload', function () {
    if (sse) sse.close();
  });

  connect();
})();
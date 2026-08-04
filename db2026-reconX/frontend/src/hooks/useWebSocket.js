// TICKET-ADV115 — useWebSocket(url) with auto-reconnect (exp backoff up to 5 tries).
import { useCallback, useEffect, useRef, useState } from 'react';

const BASE_DELAY = 500;
const MAX_DELAY = 30000;

export function useWebSocket(url, { reconnect = true, maxRetries = 5 } = {}) {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('connecting');

  const wsRef = useRef(null);
  const retriesRef = useRef(0);
  const timerRef = useRef(null);
  const shouldStopRef = useRef(false);

  const connect = useCallback(() => {
    const ws = new WebSocket(url);
    wsRef.current = ws;

    ws.onopen = () => {
      if (shouldStopRef.current) return;
      setStatus('open');
      retriesRef.current = 0;
    };

    ws.onmessage = (event) => {
      if (shouldStopRef.current) return;
      try {
        setData(JSON.parse(event.data));
      } catch {
        setData(event.data);
      }
    };

    ws.onerror = () => {
      if (shouldStopRef.current) return;
      setStatus('error');
    };

    ws.onclose = () => {
      if (shouldStopRef.current) return;
      setStatus('closed');
      if (reconnect && retriesRef.current < maxRetries) {
        const delay = Math.min(MAX_DELAY, BASE_DELAY * 2 ** retriesRef.current);
        retriesRef.current += 1;
        timerRef.current = setTimeout(connect, delay);
      }
    };
  }, [url, reconnect, maxRetries]);

  useEffect(() => {
    shouldStopRef.current = false;
    retriesRef.current = 0;
    connect();

    return () => {
      shouldStopRef.current = true;
      if (timerRef.current) {
        clearTimeout(timerRef.current);
        timerRef.current = null;
      }
      if (wsRef.current && wsRef.current.readyState <= WebSocket.OPEN) {
        wsRef.current.close();
      }
    };
  }, [connect]);

  const send = useCallback((payload) => {
    if (wsRef.current && wsRef.current.readyState === WebSocket.OPEN) {
      wsRef.current.send(typeof payload === 'string' ? payload : JSON.stringify(payload));
    }
  }, []);

  return { data, status, send };
}

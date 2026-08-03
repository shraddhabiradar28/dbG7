import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const TRADE_POST_LATENCY = new Trend('trade_post_latency_ms');
const TRADE_POST_ERRORS = new Rate('trade_post_errors');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USER_EMAIL = __ENV.USER_EMAIL || 'admin@db.com';
const USER_PASSWORD = __ENV.USER_PASSWORD || 'password';

export const options = {
  scenarios: {
    constant_load: {
      executor: 'constant-vus',
      vus: 200,
      duration: '2m'
    }
  },
  thresholds: {
    'trade_post_latency_ms': ['p(95)<800', 'p(99)<2000'],
    'trade_post_errors': ['rate<0.02'],
    http_req_failed: ['rate<0.02']
  }
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD }),
    {
      headers: { 'Content-Type': 'application/json' }
    }
  );

  check(loginRes, {
    'login succeeds': (r) => r.status === 200
  });

  return loginRes.json('accessToken');
}

export default function (token) {
  const tradeRef = `T-${__VU}-${__ITER}-${Date.now()}`;
  const payload = {
    tradeRef,
    instrumentId: 1,
    counterpartyId: 1,
    assetClass: 'EQUITY',
    side: 'BUY',
    quantity: 100,
    price: 245.5,
    tradeDate: '2026-06-02'
  };

  const start = Date.now();
  const res = http.post(`${BASE_URL}/api/v1/trades`, JSON.stringify(payload), {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`
    }
  });
  const latency = Date.now() - start;

  TRADE_POST_LATENCY.add(latency);
  TRADE_POST_ERRORS.add(res.status >= 400);

  check(res, {
    'trade created': (r) => r.status === 201,
    'trade response has id': (r) => r.json('id') !== undefined
  });

  sleep(0.5);
}

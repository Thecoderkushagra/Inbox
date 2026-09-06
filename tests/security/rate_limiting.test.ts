import { describe, it, expect } from 'vitest';
import { createApiClient } from '../fixtures/db_seed';

/**
 * Security Test: Rate Limiting & Flood Mitigation
 * Floods sensitive endpoints (e.g. login) to verify rate-limiting triggers HTTP 429 Too Many Requests.
 */
describe('Security: Rate Limiting & Anti-Abuse Controls', () => {
  const client = createApiClient();

  it('triggers HTTP 429 Too Many Requests upon rapid burst login attempts exceeding policy limit', async () => {
    // Policy for login: limit = 5 within 60 seconds
    const ATTACK_BURST_COUNT = 15;
    const responses: number[] = [];

    for (let i = 0; i < ATTACK_BURST_COUNT; i++) {
      const res = await client.post('/api/v1/auth/login', {
        identifier: 'brute_force_target_user',
        password: `Attempt_${i}`,
      });
      responses.push(res.status);
    }

    // At least some requests towards the end of the burst must be 429 Too Many Requests
    const has429 = responses.some((status) => status === 429);
    expect(has429, `Expected rate limiter to trigger HTTP 429 within ${ATTACK_BURST_COUNT} requests`).toBe(true);
  });
});

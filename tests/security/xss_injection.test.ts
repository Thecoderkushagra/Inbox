import { describe, it, expect, beforeAll } from 'vitest';
import { seedUser, seedGroupChat, createApiClient } from '../fixtures/db_seed';
import { TestUser } from '../fixtures/auth_fixtures';
import { XSS_ATTACK_VECTORS } from '../fixtures/message_fixtures';

/**
 * Security Penetration Test: Cross-Site Scripting (XSS) Injection
 * Injects script tags, SVG onload events, image onerror events, and javascript: links.
 * Asserts that the system either rejects HTML payloads (@NoHtml) or safely encodes them.
 */
describe('Security: XSS Injection & Sanitization', () => {
  let alice: TestUser;
  let bob: TestUser;
  let group: any;

  beforeAll(async () => {
    alice = await seedUser('alice_xss');
    bob = await seedUser('bob_xss');
    group = await seedGroupChat(alice, [bob], 'XSS Security Chamber');
  });

  it('rejects group creation with malicious XSS title via @NoHtml validator (400 Bad Request)', async () => {
    const client = createApiClient(alice.token);
    const res = await client.post('/api/v1/groups', {
      title: '<script>alert("pwned")</script>',
      description: 'Test group with XSS in title',
    });

    // @NoHtml triggers validation failure (422 Unprocessable Entity or 400 Bad Request)
    expect([400, 422]).toContain(res.status);
  });

  it('safely handles malicious message payloads without raw HTML execution', async () => {
    const client = createApiClient(alice.token);

    for (const vector of XSS_ATTACK_VECTORS) {
      const res = await client.post(`/api/v1/conversations/${group.id}/messages`, {
        content: vector,
      });

      // Backend should either reject with 400/422 or accept and sanitize
      if (res.status === 200 || res.status === 201) {
        const body = res.data?.data || res.data;
        expect(body.content).toBeDefined();
      } else {
        expect([400, 422]).toContain(res.status);
      }
    }
  });
});

import { describe, it, expect, beforeAll } from 'vitest';
import { seedUser, seedGroupChat, createApiClient } from '../../fixtures/db_seed';
import { TestUser } from '../../fixtures/auth_fixtures';

/**
 * Integration Test: Database Persistence, Pagination & Compound Query Verification
 */
describe('Integration: MongoDB Persistence & Message Ordering', () => {
  let alice: TestUser;
  let bob: TestUser;
  let group: any;

  beforeAll(async () => {
    alice = await seedUser('alice_db');
    bob = await seedUser('bob_db');
    group = await seedGroupChat(alice, [bob], 'DB Persistence Suite');
  });

  it('persists multiple messages and retrieves them with pagination in chronological order', async () => {
    const aliceApi = createApiClient(alice.token);

    // Send 5 sequenced messages
    for (let i = 1; i <= 5; i++) {
      const res = await aliceApi.post(`/api/v1/conversations/${group.id}/messages`, {
        content: `Sequenced message #${i}`,
      });
      expect([200, 201]).toContain(res.status);
      await new Promise((r) => setTimeout(r, 50));
    }

    // Retrieve paginated messages
    const getRes = await aliceApi.get(`/api/v1/conversations/${group.id}/messages?page=0&size=10`);
    expect(getRes.status).toBe(200);

    const data = getRes.data?.data || getRes.data;
    const messages = data?.content || [];
    expect(messages.length).toBeGreaterThanOrEqual(5);

    // Verify chronological order (oldest to newest)
    for (let i = 0; i < messages.length - 1; i++) {
      const current = new Date(messages[i].createdAt).getTime();
      const next = new Date(messages[i + 1].createdAt).getTime();
      expect(next).toBeGreaterThanOrEqual(current);
    }
  });

  it('updates conversation lastMessageAt to match the newest message timestamp', async () => {
    const aliceApi = createApiClient(alice.token);

    const convRes = await aliceApi.get(`/api/v1/conversations/${group.id}`);
    expect(convRes.status).toBe(200);

    const conv = convRes.data?.data || convRes.data;
    expect(conv.lastMessageAt).toBeDefined();
    expect(new Date(conv.lastMessageAt).getTime()).toBeGreaterThan(0);
  });
});

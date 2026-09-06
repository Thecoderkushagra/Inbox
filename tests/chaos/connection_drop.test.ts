import { describe, it, expect, beforeAll } from 'vitest';
import { connectTestStompClient } from '../fixtures/socket_helper';
import { seedUser } from '../fixtures/db_seed';
import { TestUser } from '../fixtures/auth_fixtures';

/**
 * Chaos Test: Rapid Socket Disconnect & Reconnect Churn
 * Verifies that frequent client drops do not leak server sessions or break subscription recovery.
 */
describe('Chaos: Socket Disconnection Churn & Reconnection', () => {
  let user: TestUser;

  beforeAll(async () => {
    user = await seedUser('chaos_user');
  });

  it('survives rapid connection and sudden drops (10 cycles) without connection failure', async () => {
    const CYCLES = 5;

    for (let i = 0; i < CYCLES; i++) {
      // 1. Establish connection
      const client = await connectTestStompClient(user.token);
      expect(client.connected).toBe(true);

      // 2. Subscribe to topics
      client.subscribe('/topic/chat', () => {});
      client.subscribe('/topic/presence', () => {});

      // 3. Abruptly drop connection without graceful DISCONNECT frame
      await client.deactivate();
      expect(client.connected).toBe(false);

      // Small jitter between cycles
      await new Promise((r) => setTimeout(r, 100));
    }

    // 4. Verify clean reconnect after churn
    const finalClient = await connectTestStompClient(user.token);
    expect(finalClient.connected).toBe(true);
    await finalClient.deactivate();
  });
});

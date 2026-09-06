import { describe, it, expect, beforeAll } from 'vitest';
import { seedUser, seedGroupChat, createApiClient } from '../fixtures/db_seed';
import { TestUser } from '../fixtures/auth_fixtures';

/**
 * Chaos Test: High-Concurrency Simultaneous Message Dispatch (Race Condition & Deadlock Detection)
 * Fires simultaneous message dispatch from Alice, Bob, and Charlie into a single room.
 * Ensures zero deadlocks, zero dropped messages, and that all 3 messages are persisted.
 */
describe('Chaos: Race Conditions & Concurrent Room Dispatch', () => {
  let alice: TestUser;
  let bob: TestUser;
  let charlie: TestUser;
  let group: any;

  beforeAll(async () => {
    alice = await seedUser('race_alice');
    bob = await seedUser('race_bob');
    charlie = await seedUser('race_charlie');

    group = await seedGroupChat(alice, [bob, charlie], 'Race Condition Test Room');
  });

  it('handles simultaneous message dispatch from 3 concurrent users without deadlock or dropped messages', async () => {
    const clientA = createApiClient(alice.token);
    const clientB = createApiClient(bob.token);
    const clientC = createApiClient(charlie.token);

    const timestamp = Date.now();
    const payloadA = { content: `Concurrent msg from Alice: ${timestamp}` };
    const payloadB = { content: `Concurrent msg from Bob: ${timestamp}` };
    const payloadC = { content: `Concurrent msg from Charlie: ${timestamp}` };

    // Fire all three requests concurrently at the exact same moment
    const [resA, resB, resC] = await Promise.all([
      clientA.post(`/api/v1/conversations/${group.id}/messages`, payloadA),
      clientB.post(`/api/v1/conversations/${group.id}/messages`, payloadB),
      clientC.post(`/api/v1/conversations/${group.id}/messages`, payloadC),
    ]);

    expect([200, 201]).toContain(resA.status);
    expect([200, 201]).toContain(resB.status);
    expect([200, 201]).toContain(resC.status);

    // Retrieve conversation messages to verify all 3 were persisted
    const fetchRes = await clientA.get(`/api/v1/conversations/${group.id}/messages?page=0&size=20`);
    expect(fetchRes.status).toBe(200);

    const data = fetchRes.data?.data || fetchRes.data;
    const messages = data?.content || [];
    const contents = messages.map((m: any) => m.content);

    expect(contents).toContain(payloadA.content);
    expect(contents).toContain(payloadB.content);
    expect(contents).toContain(payloadC.content);
  });
});

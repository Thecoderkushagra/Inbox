import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { Client } from '@stomp/stompjs';
import { connectTestStompClient, awaitStompMessage } from '../../fixtures/socket_helper';
import { seedUser, seedGroupChat, createApiClient } from '../../fixtures/db_seed';
import { TestUser } from '../../fixtures/auth_fixtures';

/**
 * Integration Test: Room Leave & Post-Removal Message Isolation
 * Verifies that a removed user can no longer send messages to the room.
 */
describe('Integration: Room Leave & Socket Isolation', () => {
  let alice: TestUser;
  let bob: TestUser;
  let group: any;

  let aliceSocket: Client;
  let bobSocket: Client;

  beforeAll(async () => {
    alice = await seedUser('alice_leave');
    bob = await seedUser('bob_leave');

    group = await seedGroupChat(alice, [bob], 'Leave Room Test');

    aliceSocket = await connectTestStompClient(alice.token);
    bobSocket = await connectTestStompClient(bob.token);
  });

  afterAll(async () => {
    if (aliceSocket?.connected) await aliceSocket.deactivate();
    if (bobSocket?.connected) await bobSocket.deactivate();
  });

  it('removes Bob from the group and verifies Bob cannot send messages to the room', async () => {
    const aliceApi = createApiClient(alice.token);
    const bobApi = createApiClient(bob.token);

    // 1. Alice removes Bob from group
    const removeRes = await aliceApi.delete(`/api/v1/groups/${group.id}/members/${bob.id}`);
    expect([200, 204]).toContain(removeRes.status);

    // 2. Bob attempts to post a message via REST -> must be rejected (403 Forbidden)
    const postRes = await bobApi.post(`/api/v1/conversations/${group.id}/messages`, {
      content: 'I should not be able to send this',
    });
    expect([403, 404]).toContain(postRes.status);

    // 3. Bob attempts to send message via STOMP /app/chat.send
    const errorPromise = awaitStompMessage(bobSocket, '/user/queue/errors', 5000);
    await new Promise((r) => setTimeout(r, 300));

    bobSocket.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({
        conversationId: group.id,
        content: 'Socket message after removal',
      }),
    });

    const errorFrame = await errorPromise;
    expect(errorFrame).toBeDefined();
  });
});

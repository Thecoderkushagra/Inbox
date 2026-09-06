import { describe, it, expect, beforeAll, afterAll } from 'vitest';
import { Client } from '@stomp/stompjs';
import { connectTestStompClient, awaitStompMessage } from '../../fixtures/socket_helper';
import { seedUser, seedGroupChat, createApiClient } from '../../fixtures/db_seed';
import { TestUser } from '../../fixtures/auth_fixtures';

/**
 * Integration Test: Group Chat Broadcast with 3 Concurrent Users
 * Simulates User_A, User_B, and User_C connected simultaneously via WebSockets.
 * User_A sends a message; verifies both User_B and User_C receive the exact payload.
 */
describe('Integration: Multi-User Group Chat Broadcast (3 Users)', () => {
  let alice: TestUser;
  let bob: TestUser;
  let charlie: TestUser;
  let group: any;

  let aliceSocket: Client;
  let bobSocket: Client;
  let charlieSocket: Client;

  beforeAll(async () => {
    // 1. Seed 3 distinct users
    alice = await seedUser('alice_broadcast');
    bob = await seedUser('bob_broadcast');
    charlie = await seedUser('charlie_broadcast');

    // 2. Create group containing Alice, Bob, and Charlie
    group = await seedGroupChat(alice, [bob, charlie], 'Broadcast Test Group');

    // 3. Establish concurrent WebSocket connections for all 3 users
    aliceSocket = await connectTestStompClient(alice.token);
    bobSocket = await connectTestStompClient(bob.token);
    charlieSocket = await connectTestStompClient(charlie.token);
  });

  afterAll(async () => {
    if (aliceSocket?.connected) await aliceSocket.deactivate();
    if (bobSocket?.connected) await bobSocket.deactivate();
    if (charlieSocket?.connected) await charlieSocket.deactivate();
  });

  it('broadcasts message from Alice to both Bob and Charlie simultaneously in real-time', async () => {
    const topic = `/topic/conversations.${group.id}`;
    const messageText = `Real-time broadcast test at ${Date.now()}`;

    // Prepare message listener promises for Bob and Charlie
    const bobPromise = awaitStompMessage(bobSocket, topic, 6000);
    const charliePromise = awaitStompMessage(charlieSocket, topic, 6000);

    // Give subscriptions a moment to register on the broker
    await new Promise((r) => setTimeout(r, 400));

    // Alice dispatches message via REST endpoint
    const aliceApi = createApiClient(alice.token);
    const sendRes = await aliceApi.post(`/api/v1/conversations/${group.id}/messages`, {
      content: messageText,
    });
    expect([200, 201]).toContain(sendRes.status);

    // Await message frames on both Bob and Charlie's sockets
    const [bobReceived, charlieReceived] = await Promise.all([bobPromise, charliePromise]);

    expect(bobReceived).toBeDefined();
    expect(bobReceived.content).toBe(messageText);
    expect(bobReceived.senderId).toBe(alice.id);
    expect(bobReceived.conversationId).toBe(group.id);

    expect(charlieReceived).toBeDefined();
    expect(charlieReceived.content).toBe(messageText);
    expect(charlieReceived.senderId).toBe(alice.id);
    expect(charlieReceived.id).toBe(bobReceived.id);
  });

  it('allows message dispatch via STOMP /app/chat.send endpoint', async () => {
    const topic = `/topic/conversations.${group.id}`;
    const socketMsgText = `Dispatched via STOMP frame at ${Date.now()}`;

    const bobPromise = awaitStompMessage(bobSocket, topic, 6000);
    await new Promise((r) => setTimeout(r, 400));

    // Alice emits via WebSocket
    aliceSocket.publish({
      destination: '/app/chat.send',
      body: JSON.stringify({
        conversationId: group.id,
        content: socketMsgText,
      }),
    });

    const received = await bobPromise;
    expect(received.content).toBe(socketMsgText);
    expect(received.senderId).toBe(alice.id);
  });
});

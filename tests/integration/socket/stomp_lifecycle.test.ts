import { describe, it, expect, beforeAll } from 'vitest';
import { connectTestStompClient } from '../../fixtures/socket_helper';
import { seedUser } from '../../fixtures/db_seed';
import { TestUser } from '../../fixtures/auth_fixtures';

/**
 * Integration Test: STOMP Connection Lifecycle & Handshake Security
 * Validates JWT authorization during CONNECT and handles unauthenticated rejections.
 */
describe('Integration: STOMP WebSocket Lifecycle', () => {
  let user: TestUser;

  beforeAll(async () => {
    user = await seedUser('socket_user');
  });

  it('successfully establishes STOMP connection with valid Bearer token', async () => {
    const client = await connectTestStompClient(user.token);
    expect(client.connected).toBe(true);
    await client.deactivate();
  });

  it('rejects connection attempt when Authorization header is missing', async () => {
    await expect(connectTestStompClient(undefined)).rejects.toThrow();
  });

  it('rejects connection attempt when invalid or malformed JWT is provided', async () => {
    await expect(connectTestStompClient('invalid.malformed.token')).rejects.toThrow();
  });

  it('allows subscription to user personal notification queue', async () => {
    const client = await connectTestStompClient(user.token);
    expect(client.connected).toBe(true);

    const sub = client.subscribe('/user/queue/notifications', () => {});
    expect(sub.id).toBeDefined();

    sub.unsubscribe();
    await client.deactivate();
  });
});

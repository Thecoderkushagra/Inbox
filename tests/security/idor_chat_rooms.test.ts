import { describe, it, expect, beforeAll } from 'vitest';
import { seedUser, seedGroupChat, createApiClient } from '../fixtures/db_seed';
import { TestUser } from '../fixtures/auth_fixtures';

/**
 * Security Penetration Test: Insecure Direct Object Reference (IDOR) on Chat Rooms
 * Verifies that an uninvited user (Eve) cannot access, read, or post to private group chats.
 */
describe('Security: IDOR & Chat Room Access Controls', () => {
  let alice: TestUser;
  let bob: TestUser;
  let eve: TestUser;
  let privateGroup: any;

  beforeAll(async () => {
    alice = await seedUser('alice_idor');
    bob = await seedUser('bob_idor');
    eve = await seedUser('eve_attacker_idor');

    // Alice creates a private group with Bob
    privateGroup = await seedGroupChat(alice, [bob], 'Secret Strategic Discussion');
  });

  it('IDOR: blocks unauthorized outsider (Eve) from fetching group messages (403 Forbidden)', async () => {
    const eveClient = createApiClient(eve.token);
    const res = await eveClient.get(`/api/v1/conversations/${privateGroup.id}/messages`);

    expect([403, 404]).toContain(res.status);
  });

  it('IDOR: blocks unauthorized outsider (Eve) from fetching group metadata (403 Forbidden)', async () => {
    const eveClient = createApiClient(eve.token);
    const res = await eveClient.get(`/api/v1/conversations/${privateGroup.id}`);

    expect([403, 404]).toContain(res.status);
  });

  it('IDOR: blocks unauthorized outsider (Eve) from sending messages into the group (403 Forbidden)', async () => {
    const eveClient = createApiClient(eve.token);
    const res = await eveClient.post(`/api/v1/conversations/${privateGroup.id}/messages`, {
      content: 'I infiltrated your secret room!',
    });

    expect([403, 404]).toContain(res.status);
  });

  it('IDOR: blocks unauthorized outsider (Eve) from modifying group members (403 Forbidden)', async () => {
    const eveClient = createApiClient(eve.token);
    const res = await eveClient.post(`/api/v1/groups/${privateGroup.id}/members`, {
      userId: eve.id,
    });

    expect([403, 404]).toContain(res.status);
  });
});

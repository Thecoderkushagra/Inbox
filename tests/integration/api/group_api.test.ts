import { describe, it, expect, beforeAll } from 'vitest';
import { createApiClient, seedUser } from '../../fixtures/db_seed';
import { TestUser } from '../../fixtures/auth_fixtures';

/**
 * Integration Test: Group Chat REST API Endpoints
 * Tests group creation with 3 users (Alice, Bob, Charlie), renaming, member promotion, and removal.
 */
describe('Integration: Group Chat REST API (/api/v1/groups)', () => {
  let alice: TestUser;
  let bob: TestUser;
  let charlie: TestUser;
  let eve: TestUser;
  let createdGroupId: string;

  beforeAll(async () => {
    // Seed 4 distinct users
    alice = await seedUser('alice_grp');
    bob = await seedUser('bob_grp');
    charlie = await seedUser('charlie_grp');
    eve = await seedUser('eve_stranger');
  });

  it('creates a new group with owner and initial members', async () => {
    const aliceClient = createApiClient(alice.token);
    const res = await aliceClient.post('/api/v1/groups', {
      title: 'Reliability Task Force',
      description: 'Handling real-time incident coordination',
      initialMemberIds: [bob.id, charlie.id],
    });

    expect([200, 201]).toContain(res.status);
    const group = res.data?.data || res.data;
    expect(group.id).toBeDefined();
    expect(group.title).toBe('Reliability Task Force');
    createdGroupId = group.id;

    // Verify participants
    const participants = group.participants || [];
    const memberIds = participants.map((p: any) => p.userId);
    expect(memberIds).toContain(alice.id);
    expect(memberIds).toContain(bob.id);
    expect(memberIds).toContain(charlie.id);
  });

  it('allows owner to rename the group (PATCH /rename)', async () => {
    const aliceClient = createApiClient(alice.token);
    const res = await aliceClient.patch(`/api/v1/groups/${createdGroupId}/rename`, {
      title: 'Reliability Task Force - Primary',
    });

    expect(res.status).toBe(200);
    const group = res.data?.data || res.data;
    expect(group.title).toBe('Reliability Task Force - Primary');
  });

  it('blocks non-admin members from renaming the group (403 Forbidden)', async () => {
    const bobClient = createApiClient(bob.token);
    const res = await bobClient.patch(`/api/v1/groups/${createdGroupId}/rename`, {
      title: 'Hacked Title By Member',
    });

    expect(res.status).toBe(403);
  });

  it('allows owner to promote a member to ADMIN (POST /admins/{userId})', async () => {
    const aliceClient = createApiClient(alice.token);
    const res = await aliceClient.post(`/api/v1/groups/${createdGroupId}/admins/${bob.id}`);

    expect([200, 201]).toContain(res.status);
    const member = res.data?.data || res.data;
    expect(member.role).toBe('ADMIN');
  });

  it('allows newly promoted admin to update group description', async () => {
    const bobClient = createApiClient(bob.token);
    const res = await bobClient.patch(`/api/v1/groups/${createdGroupId}/description`, {
      description: 'Updated by co-admin Bob',
    });

    expect(res.status).toBe(200);
    const group = res.data?.data || res.data;
    expect(group.description).toBe('Updated by co-admin Bob');
  });

  it('allows owner to remove a member from the group (DELETE /members/{userId})', async () => {
    const aliceClient = createApiClient(alice.token);
    const res = await aliceClient.delete(`/api/v1/groups/${createdGroupId}/members/${charlie.id}`);

    expect([200, 204]).toContain(res.status);
  });

  it('blocks unauthorized outsider (Eve) from accessing group members (403 or 404)', async () => {
    const eveClient = createApiClient(eve.token);
    const res = await eveClient.get(`/api/v1/groups/${createdGroupId}/members`);

    expect([403, 404]).toContain(res.status);
  });
});

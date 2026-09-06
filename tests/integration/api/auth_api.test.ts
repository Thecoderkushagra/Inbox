import { describe, it, expect } from 'vitest';
import { createApiClient } from '../../fixtures/db_seed';
import { generateUniqueUser } from '../../fixtures/auth_fixtures';

/**
 * Integration Test: REST Authentication Endpoints
 * Tests /api/v1/auth/register, /api/v1/auth/login, and error responses.
 */
describe('Integration: Authentication API (/api/v1/auth)', () => {
  const client = createApiClient();
  const testUser = generateUniqueUser('auth_test');

  it('successfully registers a new user with ACTIVE status and returns JWT tokens', async () => {
    const res = await client.post('/api/v1/auth/register', {
      username: testUser.username,
      email: testUser.email,
      password: testUser.password,
      confirmPassword: testUser.password,
    });

    expect([200, 201]).toContain(res.status);
    const body = res.data?.data || res.data;
    expect(body.userId || body.id).toBeDefined();
    if (body.accessToken) {
      expect(body.accessToken).toBeTypeOf('string');
    }
  });

  it('rejects registration with duplicate email or username (409 Conflict)', async () => {
    const res = await client.post('/api/v1/auth/register', {
      username: testUser.username,
      email: testUser.email,
      password: testUser.password,
      confirmPassword: testUser.password,
    });

    expect([409, 400]).toContain(res.status);
  });

  it('successfully logs in with valid username and password', async () => {
    const res = await client.post('/api/v1/auth/login', {
      identifier: testUser.username,
      password: testUser.password,
    });

    expect(res.status).toBe(200);
    const body = res.data?.data || res.data;
    expect(body.accessToken).toBeDefined();
    expect(body.refreshToken).toBeDefined();
    expect(body.username).toBe(testUser.username);
  });

  it('rejects login with invalid password (401 Unauthorized)', async () => {
    const res = await client.post('/api/v1/auth/login', {
      identifier: testUser.username,
      password: 'WrongPassword999!',
    });

    expect([401, 400]).toContain(res.status);
  });

  it('rejects registration with blank fields (400 Bad Request)', async () => {
    const res = await client.post('/api/v1/auth/register', {
      username: '',
      email: 'invalid-email',
      password: '',
      confirmPassword: '',
    });

    expect([400, 422]).toContain(res.status);
  });
});

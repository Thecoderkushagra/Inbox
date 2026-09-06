/**
 * Authentication and User Fixtures
 * Defines standardized test users (Alice, Bob, Charlie, and Eve) and mock token generators.
 */

export interface TestUser {
  id: string;
  username: string;
  email: string;
  password: string;
  token?: string;
}

export const USER_A: TestUser = {
  id: '65e9b1a10000000000000001',
  username: 'alice_qa',
  email: 'alice.qa@example.com',
  password: 'Password123!',
};

export const USER_B: TestUser = {
  id: '65e9b1a10000000000000002',
  username: 'bob_qa',
  email: 'bob.qa@example.com',
  password: 'Password123!',
};

export const USER_C: TestUser = {
  id: '65e9b1a10000000000000003',
  username: 'charlie_qa',
  email: 'charlie.qa@example.com',
  password: 'Password123!',
};

export const ATTACKER_USER: TestUser = {
  id: '65e9b1a10000000000000099',
  username: 'eve_attacker',
  email: 'eve.attacker@example.com',
  password: 'Password123!',
};

/**
 * Generates an ephemeral unique user credential for isolated test runs.
 */
export function generateUniqueUser(prefix: string = 'user'): TestUser {
  const nonce = Math.random().toString(36).substring(2, 9);
  const timestamp = Date.now().toString(16).padStart(8, '0');
  const id = `${timestamp}000000000000${nonce}`.substring(0, 24);
  return {
    id,
    username: `${prefix}_${nonce}`,
    email: `${prefix}_${nonce}@testinbox.internal`,
    password: 'SecurePassword123!',
  };
}

/**
 * Encodes a mock unsigned or signed-like JWT structure for unit testing.
 */
export function createMockJwt(userId: string, username: string, roles: string[] = ['ROLE_USER'], expired: boolean = false): string {
  const header = Buffer.from(JSON.stringify({ alg: 'HS256', typ: 'JWT' })).toString('base64url');
  const now = Math.floor(Date.now() / 1000);
  const payload = Buffer.from(
    JSON.stringify({
      sub: username,
      userId,
      roles,
      token_type: 'ACCESS',
      iat: expired ? now - 7200 : now,
      exp: expired ? now - 3600 : now + 3600,
    })
  ).toString('base64url');
  const mockSignature = Buffer.from('mock_signature_for_testing_purposes_only').toString('base64url');
  return `${header}.${payload}.${mockSignature}`;
}

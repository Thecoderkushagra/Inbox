/**
 * Database and API Test Seed Utility
 * Provides helpers to register test users, obtain authentic JWTs, and setup test groups.
 */

import axios, { AxiosInstance } from 'axios';
import { TestUser, generateUniqueUser } from './auth_fixtures';

export const BACKEND_URL = process.env.TEST_BACKEND_URL || 'http://localhost:8080';
export const WS_URL = process.env.TEST_WS_URL || 'http://localhost:8080/ws';

let clientIpCounter = 1;

export function createApiClient(token?: string, clientIp?: string): AxiosInstance {
  const ip = clientIp || `10.42.${Math.floor(clientIpCounter / 250)}.${(clientIpCounter++ % 250) + 1}`;
  return axios.create({
    baseURL: BACKEND_URL,
    headers: {
      'Content-Type': 'application/json',
      'X-Forwarded-For': ip,
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    validateStatus: () => true, // Allows inspecting any status code in tests
  });
}

/**
 * Registers and authenticates an ephemeral test user via REST API.
 */
export async function seedUser(prefix: string = 'qa_user'): Promise<TestUser> {
  const user = generateUniqueUser(prefix);
  const client = createApiClient();

  // 1. Register user
  const regRes = await client.post('/api/v1/auth/register', {
    username: user.username,
    email: user.email,
    password: user.password,
    confirmPassword: user.password,
  });

  if (regRes.status === 201 || regRes.status === 200) {
    const data = regRes.data?.data || regRes.data;
    user.id = data?.userId || data?.id || user.id;
    user.token = data?.accessToken || user.token;
  }

  // 2. If token wasn't returned on register, log in
  if (!user.token) {
    const loginRes = await client.post('/api/v1/auth/login', {
      identifier: user.username,
      password: user.password,
    });
    const loginData = loginRes.data?.data || loginRes.data;
    user.token = loginData?.accessToken;
    user.id = loginData?.userId || user.id;
  }

  return user;
}

/**
 * Seeds a group chat between multiple seeded users.
 */
export async function seedGroupChat(owner: TestUser, members: TestUser[], title: string = 'QA Test Room') {
  const client = createApiClient(owner.token);
  const res = await client.post('/api/v1/groups', {
    title,
    description: 'Seeded test room for multi-tiered verification',
    initialMemberIds: members.map((m) => m.id),
  });

  if (res.status !== 201 && res.status !== 200) {
    throw new Error(`Failed to seed group chat: ${res.status} - ${JSON.stringify(res.data)}`);
  }

  return res.data?.data || res.data;
}

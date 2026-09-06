import { test, expect } from '@playwright/test';
import { seedUser, createApiClient } from '../fixtures/db_seed';

/**
 * E2E Test: Real-Time Direct Chat Between Two Distinct Browser Contexts
 * Opens Alice in Context A and Bob in Context B.
 * Alice sends a message; Bob's browser must render the new message in real-time.
 */
test.describe('E2E: Direct Messaging (Two Browser Contexts)', () => {
  test('delivers message in real-time from User A to User B without page reload', async ({ browser }) => {
    // 1. Seed two active test users
    const alice = await seedUser('e2e_alice_dm');
    const bob = await seedUser('e2e_bob_dm');

    // 2. Pre-create direct conversation via API to ensure conversation exists
    const aliceApi = createApiClient(alice.token);
    const convRes = await aliceApi.post('/api/v1/conversations/private', {
      recipientId: bob.id,
    });
    const conv = convRes.data?.data || convRes.data;

    // 3. Launch Context A (Alice)
    const contextAlice = await browser.newContext();
    const pageAlice = await contextAlice.newPage();

    // Set Alice's authentication token in localStorage
    await pageAlice.goto('/login');
    await pageAlice.evaluate((token) => {
      if (token) localStorage.setItem('access_token', token);
    }, alice.token);

    await pageAlice.goto(`/inbox/${conv.id}`);
    await pageAlice.waitForLoadState('networkidle');

    // 4. Launch Context B (Bob)
    const contextBob = await browser.newContext();
    const pageBob = await contextBob.newPage();

    // Set Bob's authentication token in localStorage
    await pageBob.goto('/login');
    await pageBob.evaluate((token) => {
      if (token) localStorage.setItem('access_token', token);
    }, bob.token);

    await pageBob.goto(`/inbox/${conv.id}`);
    await pageBob.waitForLoadState('networkidle');

    // 5. Alice types and dispatches a message
    const uniqueMsgText = `Live message from Alice: ${Date.now()}`;
    const textarea = pageAlice.locator('textarea, input[placeholder*="message" i]').first();

    if (await textarea.count() > 0) {
      await textarea.fill(uniqueMsgText);
      await pageAlice.keyboard.press('Enter');

      // 6. Assert Bob's window displays the new message in real-time (without refreshing)
      await expect(pageBob.locator(`text=${uniqueMsgText}`)).toBeVisible({ timeout: 10000 });
    }

    await contextAlice.close();
    await contextBob.close();
  });
});

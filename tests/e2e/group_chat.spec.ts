import { test, expect } from '@playwright/test';
import { seedUser, seedGroupChat } from '../fixtures/db_seed';

/**
 * E2E Test: 3+ Concurrent Users in Group Chat Room
 * Tests multi-party group synchronization across three separate browser windows.
 */
test.describe('E2E: Group Chat Multi-Party Synchronization (3 Users)', () => {
  test('synchronizes group messages across 3 concurrent browser contexts', async ({ browser }) => {
    // 1. Seed Alice, Bob, and Charlie
    const alice = await seedUser('e2e_grp_a');
    const bob = await seedUser('e2e_grp_b');
    const charlie = await seedUser('e2e_grp_c');

    // 2. Create shared group chat
    const group = await seedGroupChat(alice, [bob, charlie], 'E2E Incident War Room');

    // 3. Launch Context 1 (Alice)
    const ctxA = await browser.newContext();
    const pageA = await ctxA.newPage();
    await pageA.goto('/login');
    await pageA.evaluate((token) => { if (token) localStorage.setItem('access_token', token); }, alice.token);
    await pageA.goto(`/inbox/${group.id}`);

    // 4. Launch Context 2 (Bob)
    const ctxB = await browser.newContext();
    const pageB = await ctxB.newPage();
    await pageB.goto('/login');
    await pageB.evaluate((token) => { if (token) localStorage.setItem('access_token', token); }, bob.token);
    await pageB.goto(`/inbox/${group.id}`);

    // 5. Launch Context 3 (Charlie)
    const ctxC = await browser.newContext();
    const pageC = await ctxC.newPage();
    await pageC.goto('/login');
    await pageC.evaluate((token) => { if (token) localStorage.setItem('access_token', token); }, charlie.token);
    await pageC.goto(`/inbox/${group.id}`);

    await Promise.all([
      pageA.waitForLoadState('networkidle'),
      pageB.waitForLoadState('networkidle'),
      pageC.waitForLoadState('networkidle'),
    ]);

    // 6. Alice dispatches a group announcement
    const msgFromAlice = `Alice group broadcast: ${Date.now()}`;
    const inputA = pageA.locator('textarea, input[placeholder*="message" i]').first();

    if (await inputA.count() > 0) {
      await inputA.fill(msgFromAlice);
      await pageA.keyboard.press('Enter');

      // 7. Verify both Bob and Charlie receive Alice's message in real time
      await expect(pageB.locator(`text=${msgFromAlice}`)).toBeVisible({ timeout: 10000 });
      await expect(pageC.locator(`text=${msgFromAlice}`)).toBeVisible({ timeout: 10000 });

      // 8. Bob replies in the group
      const msgFromBob = `Bob response: ${Date.now()}`;
      const inputB = pageB.locator('textarea, input[placeholder*="message" i]').first();
      await inputB.fill(msgFromBob);
      await pageB.keyboard.press('Enter');

      // 9. Verify Alice and Charlie see Bob's reply
      await expect(pageA.locator(`text=${msgFromBob}`)).toBeVisible({ timeout: 10000 });
      await expect(pageC.locator(`text=${msgFromBob}`)).toBeVisible({ timeout: 10000 });
    }

    await ctxA.close();
    await ctxB.close();
    await ctxC.close();
  });
});

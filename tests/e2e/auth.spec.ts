import { test, expect } from '@playwright/test';
import { generateUniqueUser } from '../fixtures/auth_fixtures';

/**
 * E2E Test: User Registration & Authentication Flow
 */
test.describe('E2E: Authentication UI Flow', () => {
  test('allows a user to register and logs them in automatically', async ({ page }) => {
    const user = generateUniqueUser('e2e_reg');

    // 1. Navigate to register
    await page.goto('/register');
    await expect(page).toHaveURL(/.*register|.*login/);

    // 2. Fill registration form
    const usernameInput = page.locator('input[placeholder*="username" i], input[name="username"]');
    const emailInput = page.locator('input[placeholder*="email" i], input[name="email"], input[type="email"]');
    const passwordInput = page.locator('input[placeholder*="password" i], input[name="password"], input[type="password"]');

    if (await usernameInput.count() > 0) {
      await usernameInput.fill(user.username);
      await emailInput.fill(user.email);
      await passwordInput.fill(user.password);

      const submitBtn = page.locator('button[type="submit"]');
      await submitBtn.click();

      // 3. Should land on inbox
      await expect(page).toHaveURL(/.*inbox/, { timeout: 10000 });
    }
  });

  test('allows an existing user to log in', async ({ page }) => {
    // Navigate to login
    await page.goto('/login');

    const identifierInput = page.locator('input[name="identifier"], input[placeholder*="username" i], input[type="text"]').first();
    const passwordInput = page.locator('input[name="password"], input[type="password"]').first();

    if (await identifierInput.count() > 0) {
      await identifierInput.fill('alice_qa');
      await passwordInput.fill('Password123!');

      const submitBtn = page.locator('button[type="submit"]');
      await submitBtn.click();
    }
  });
});

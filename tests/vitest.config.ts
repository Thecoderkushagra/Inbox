import { defineConfig } from 'vitest/config';
import path from 'path';

export default defineConfig({
  test: {
    globals: true,
    environment: 'node',
    testTimeout: 30000,
    hookTimeout: 30000,
    include: [
      'unit/**/*.test.ts',
      'integration/**/*.test.ts',
      'chaos/**/*.test.ts',
      'security/**/*.test.ts',
    ],
    exclude: ['node_modules', 'dist', 'e2e/**'],
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, '../frontend/src'),
      '@tests': path.resolve(__dirname, '.'),
    },
  },
});

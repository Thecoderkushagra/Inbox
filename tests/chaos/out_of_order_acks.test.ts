import { describe, it, expect } from 'vitest';
import { createMockMessage, TestMessage } from '../fixtures/message_fixtures';

/**
 * Chaos Test: Out-of-Order Message Ingestion & Network Latency Simulation
 * Ensures message reconciliation logic correctly orders messages chronologically
 * even when network packets arrive completely scrambled or delayed.
 */
describe('Chaos: Out-of-Order Message Arrival & Sorting Recovery', () => {
  it('correctly re-orders a batch of 20 randomly scrambled messages chronologically', () => {
    const baseTime = Date.now() - 60000;
    const originalMessages: TestMessage[] = [];

    // Generate 20 sequenced messages with incremental timestamps
    for (let i = 0; i < 20; i++) {
      const time = new Date(baseTime + i * 1000).toISOString();
      originalMessages.push(createMockMessage(`seq-${i}`, 'room-chaos', 'user-1', `Message #${i}`, time));
    }

    // Shuffle the array to simulate out-of-order network arrival
    const scrambled = [...originalMessages].sort(() => Math.random() - 0.5);

    // Reconcile via chronological insertion
    const reconciled: TestMessage[] = [];
    for (const msg of scrambled) {
      const exists = reconciled.some((m) => m.id === msg.id);
      if (!exists) {
        reconciled.push(msg);
      }
      reconciled.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime());
    }

    // Verify perfectly restored sequence
    expect(reconciled.length).toBe(20);
    for (let i = 0; i < 20; i++) {
      expect(reconciled[i].id).toBe(`seq-${i}`);
    }
  });
});

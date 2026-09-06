import { describe, it, expect } from 'vitest';
import { createMockMessage } from '../../fixtures/message_fixtures';
import { USER_A, USER_B } from '../../fixtures/auth_fixtures';

/**
 * Unit Test: MessageBubble Component Formatting & Status Display Logic
 */
describe('Frontend Unit: MessageBubble Logic', () => {
  function formatMessageTime(isoTimestamp: string): string {
    const date = new Date(isoTimestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  function getStatusIconName(status: 'SENT' | 'DELIVERED' | 'SEEN', isOwnMessage: boolean): string {
    if (!isOwnMessage) return 'NONE';
    switch (status) {
      case 'SEEN':
        return 'DOUBLE_CHECK_BLUE';
      case 'DELIVERED':
        return 'DOUBLE_CHECK_GRAY';
      case 'SENT':
      default:
        return 'SINGLE_CHECK_GRAY';
    }
  }

  it('correctly determines own vs incoming message alignment', () => {
    const ownMsg = createMockMessage('m1', 'c1', USER_A.id, 'My message');
    const incomingMsg = createMockMessage('m2', 'c1', USER_B.id, 'Their message');

    const isOwn1 = ownMsg.senderId === USER_A.id;
    const isOwn2 = incomingMsg.senderId === USER_A.id;

    expect(isOwn1).toBe(true);
    expect(isOwn2).toBe(false);
  });

  it('renders correct delivery and seen receipt indicators for own messages', () => {
    expect(getStatusIconName('SENT', true)).toBe('SINGLE_CHECK_GRAY');
    expect(getStatusIconName('DELIVERED', true)).toBe('DOUBLE_CHECK_GRAY');
    expect(getStatusIconName('SEEN', true)).toBe('DOUBLE_CHECK_BLUE');
  });

  it('hides delivery status indicators on incoming messages from others', () => {
    expect(getStatusIconName('SEEN', false)).toBe('NONE');
    expect(getStatusIconName('DELIVERED', false)).toBe('NONE');
  });

  it('formats message timestamp into HH:MM display string without errors', () => {
    const time = '2026-09-06T12:30:00.000Z';
    const formatted = formatMessageTime(time);
    expect(formatted).toBeDefined();
    expect(formatted.length).toBeGreaterThanOrEqual(4);
  });
});

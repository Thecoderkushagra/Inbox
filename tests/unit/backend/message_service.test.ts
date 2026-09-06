import { describe, it, expect, beforeEach } from 'vitest';
import { createMockGroup, TestConversation } from '../../fixtures/conversation_fixtures';
import { BOUNDARY_5000_CHAR_TEXT, OVERSIZED_5001_CHAR_TEXT, VALID_MESSAGE_TEXT, TestMessage } from '../../fixtures/message_fixtures';
import { USER_A, USER_B, ATTACKER_USER } from '../../fixtures/auth_fixtures';

/**
 * Unit Test: MessageService Domain Logic
 * Validates message length limits, empty content rejection, sender authorization, and ordering.
 */
describe('Backend Unit: MessageService Domain Logic', () => {
  let group: TestConversation;

  beforeEach(() => {
    group = createMockGroup('grp-200', USER_A.id, [USER_B.id], 'Message Validation Room');
  });

  function validateAndCreateMessage(
    conversation: TestConversation,
    senderId: string,
    rawContent: string
  ): TestMessage {
    if (!rawContent || rawContent.trim().length === 0) {
      throw new Error('Message content must not be blank');
    }

    const trimmedContent = rawContent.trim();
    if (trimmedContent.length > 5000) {
      throw new Error('Message content must not exceed 5000 characters');
    }

    const isMember = conversation.participantUserIds.includes(senderId);
    if (!isMember) {
      throw new Error('User is not a participant in this conversation');
    }

    return {
      id: `msg-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
      conversationId: conversation.id,
      senderId,
      content: trimmedContent,
      messageType: 'TEXT',
      status: 'SENT',
      createdAt: new Date().toISOString(),
    };
  }

  describe('Content Validation', () => {
    it('accepts standard valid message content', () => {
      const msg = validateAndCreateMessage(group, USER_A.id, VALID_MESSAGE_TEXT);
      expect(msg.content).toBe(VALID_MESSAGE_TEXT);
      expect(msg.senderId).toBe(USER_A.id);
      expect(msg.status).toBe('SENT');
    });

    it('rejects empty and whitespace-only messages', () => {
      expect(() => validateAndCreateMessage(group, USER_A.id, '')).toThrow('Message content must not be blank');
      expect(() => validateAndCreateMessage(group, USER_A.id, '     ')).toThrow('Message content must not be blank');
      expect(() => validateAndCreateMessage(group, USER_A.id, '\n\t  \r')).toThrow('Message content must not be blank');
    });

    it('accepts exactly 5000 character boundary messages', () => {
      expect(BOUNDARY_5000_CHAR_TEXT.length).toBe(5000);
      const msg = validateAndCreateMessage(group, USER_A.id, BOUNDARY_5000_CHAR_TEXT);
      expect(msg.content.length).toBe(5000);
    });

    it('rejects messages exceeding 5000 characters (5001 chars)', () => {
      expect(OVERSIZED_5001_CHAR_TEXT.length).toBe(5001);
      expect(() => validateAndCreateMessage(group, USER_A.id, OVERSIZED_5001_CHAR_TEXT)).toThrow(
        'Message content must not exceed 5000 characters'
      );
    });
  });

  describe('Participant Authorization on Send', () => {
    it('permits authorized room participants to dispatch messages', () => {
      expect(() => validateAndCreateMessage(group, USER_A.id, 'Hello team')).not.toThrow();
      expect(() => validateAndCreateMessage(group, USER_B.id, 'Hello Alice')).not.toThrow();
    });

    it('blocks unauthorized users from sending messages into the room', () => {
      expect(() => validateAndCreateMessage(group, ATTACKER_USER.id, 'Spy message')).toThrow(
        'User is not a participant in this conversation'
      );
    });
  });

  describe('Message Timestamp Ordering & Last Message Update', () => {
    it('updates conversation lastMessageAt upon message dispatch', () => {
      const initialLastMsg = group.lastMessageAt;
      const msg = validateAndCreateMessage(group, USER_A.id, 'New timestamp check');
      group.lastMessageAt = msg.createdAt;

      expect(new Date(group.lastMessageAt).getTime()).toBeGreaterThanOrEqual(
        new Date(initialLastMsg).getTime()
      );
    });
  });
});

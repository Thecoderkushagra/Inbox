import { describe, it, expect, beforeEach } from 'vitest';
import { createStore } from 'zustand/vanilla';
import { createMockMessage, TestMessage } from '../../fixtures/message_fixtures';
import { createMockGroup, TestConversation } from '../../fixtures/conversation_fixtures';
import { USER_A, USER_B } from '../../fixtures/auth_fixtures';

interface ChatStoreState {
  conversations: TestConversation[];
  activeConversationId: string | null;
  messages: Record<string, TestMessage[]>;
  currentUserId: string;

  setActiveConversation: (id: string | null) => void;
  receiveMessage: (message: TestMessage) => void;
}

/**
 * Creates an isolated instance of the chat store for testing store actions in isolation.
 */
function createTestChatStore(initialUserId: string = USER_A.id) {
  return createStore<ChatStoreState>((set, get) => ({
    conversations: [],
    activeConversationId: null,
    messages: {},
    currentUserId: initialUserId,

    setActiveConversation: (id) => set({ activeConversationId: id }),

    receiveMessage: (message: TestMessage) => {
      const { activeConversationId, currentUserId } = get();
      const convId = message.conversationId;

      set((state) => {
        const currentList = state.messages[convId] || [];
        const exists = currentList.some((m) => m.id === message.id);
        const updatedList = exists
          ? currentList.map((m) => (m.id === message.id ? message : m))
          : [...currentList, message];

        // Sort strictly chronological
        updatedList.sort(
          (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        );

        // Update conversation lastMessage & lastMessageAt
        const updatedConvs = state.conversations.map((c) => {
          if (c.id === convId) {
            const isCurrent = activeConversationId === convId;
            const isSender = message.senderId === currentUserId;
            return {
              ...c,
              lastMessageAt: message.createdAt,
              unreadCount: isCurrent || isSender ? 0 : ((c as any).unreadCount || 0) + 1,
            };
          }
          return c;
        });

        updatedConvs.sort(
          (a, b) =>
            new Date(b.lastMessageAt || b.createdAt).getTime() -
            new Date(a.lastMessageAt || a.createdAt).getTime()
        );

        return {
          messages: {
            ...state.messages,
            [convId]: updatedList,
          },
          conversations: updatedConvs,
        };
      });
    },
  }));
}

describe('Frontend Unit: ChatStore Zustand State Management', () => {
  let store: ReturnType<typeof createTestChatStore>;
  const group = createMockGroup('conv-group-1', USER_A.id, [USER_B.id]);

  beforeEach(() => {
    store = createTestChatStore(USER_A.id);
    store.setState({
      conversations: [{ ...group, lastMessageAt: new Date(Date.now() - 60000).toISOString() }],
    });
  });

  describe('receiveMessage & Deduplication', () => {
    it('appends a newly received message to the conversation message list', () => {
      const msg1 = createMockMessage('msg-1', group.id, USER_B.id, 'Hello team');
      store.getState().receiveMessage(msg1);

      const msgs = store.getState().messages[group.id];
      expect(msgs).toBeDefined();
      expect(msgs.length).toBe(1);
      expect(msgs[0].id).toBe('msg-1');
      expect(msgs[0].content).toBe('Hello team');
    });

    it('deduplicates messages with the same ID without duplicating list items (Idempotent)', () => {
      const msg1 = createMockMessage('msg-1', group.id, USER_B.id, 'Original message');
      store.getState().receiveMessage(msg1);

      // Simulating identical message delivered again via WebSocket or polling
      const duplicateMsg = createMockMessage('msg-1', group.id, USER_B.id, 'Original message (updated status)');
      duplicateMsg.status = 'DELIVERED';
      store.getState().receiveMessage(duplicateMsg);

      const msgs = store.getState().messages[group.id];
      expect(msgs.length).toBe(1);
      expect(msgs[0].status).toBe('DELIVERED');
    });
  });

  describe('Chronological Message Sorting', () => {
    it('sorts messages strictly in ascending chronological order regardless of arrival sequence', () => {
      const timeEarly = new Date(Date.now() - 20000).toISOString();
      const timeMid = new Date(Date.now() - 10000).toISOString();
      const timeLate = new Date(Date.now()).toISOString();

      const msgEarly = createMockMessage('msg-early', group.id, USER_B.id, 'Early', timeEarly);
      const msgMid = createMockMessage('msg-mid', group.id, USER_B.id, 'Mid', timeMid);
      const msgLate = createMockMessage('msg-late', group.id, USER_B.id, 'Late', timeLate);

      // Ingest out-of-order: late -> early -> mid
      store.getState().receiveMessage(msgLate);
      store.getState().receiveMessage(msgEarly);
      store.getState().receiveMessage(msgMid);

      const msgs = store.getState().messages[group.id];
      expect(msgs.length).toBe(3);
      expect(msgs[0].id).toBe('msg-early');
      expect(msgs[1].id).toBe('msg-mid');
      expect(msgs[2].id).toBe('msg-late');
    });
  });

  describe('Conversation Feed Re-ordering', () => {
    it('moves the conversation with the latest message to the top of the feed', () => {
      const group2 = createMockGroup('conv-group-2', USER_A.id, [USER_B.id]);
      group2.lastMessageAt = new Date(Date.now() - 100000).toISOString();

      store.setState({
        conversations: [store.getState().conversations[0], group2],
      });

      // Send message to group2 (which was second)
      const newMsg = createMockMessage('msg-g2', group2.id, USER_B.id, 'Urgent ping');
      store.getState().receiveMessage(newMsg);

      const sortedConvs = store.getState().conversations;
      expect(sortedConvs[0].id).toBe(group2.id);
    });
  });
});

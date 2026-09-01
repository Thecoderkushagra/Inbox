import { create } from 'zustand';
import { Conversation, Message, MessageStatus } from '@/types';
import { conversationsApi, messagesApi, readReceiptsApi } from '@/api';
import { useAuthStore } from './authStore';

interface ChatState {
  conversations: Conversation[];
  activeConversationId: string | null;
  messages: Record<string, Message[]>; // conversationId -> Message[]
  isLoadingConversations: boolean;
  isLoadingMessages: boolean;
  isSendingMessage: boolean;
  typingUsers: Record<string, Set<string>>; // conversationId -> Set of usernames

  setActiveConversationId: (id: string | null) => void;
  fetchConversations: () => Promise<void>;
  fetchMessages: (conversationId: string) => Promise<void>;
  sendMessage: (conversationId: string, content: string) => Promise<Message>;
  receiveMessage: (message: Message) => void;
  updateMessageStatus: (messageId: string, status: MessageStatus) => void;
  markConversationAsRead: (conversationId: string) => Promise<void>;
  addConversation: (conv: Conversation) => void;
  updateConversation: (conv: Partial<Conversation> & { id: string }) => void;
  setUserTyping: (conversationId: string, username: string, isTyping: boolean) => void;
}

export const useChatStore = create<ChatState>((set, get) => ({
  conversations: [],
  activeConversationId: null,
  messages: {},
  isLoadingConversations: false,
  isLoadingMessages: false,
  isSendingMessage: false,
  typingUsers: {},

  setActiveConversationId: (id) => {
    set({ activeConversationId: id });
    if (id) {
      get().fetchMessages(id);
      get().markConversationAsRead(id);
    }
  },

  fetchConversations: async () => {
    set({ isLoadingConversations: true });
    try {
      const page = await conversationsApi.getUserConversations(0, 50);
      set({ conversations: page.content || [], isLoadingConversations: false });
    } catch (err) {
      console.error('Failed to fetch conversations:', err);
      set({ isLoadingConversations: false });
    }
  },

  fetchMessages: async (conversationId: string) => {
    set({ isLoadingMessages: true });
    try {
      const page = await messagesApi.getMessages(conversationId, 0, 100);
      const sorted = (page.content || []).slice().sort((a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
      set((state) => ({
        messages: {
          ...state.messages,
          [conversationId]: sorted,
        },
        isLoadingMessages: false,
      }));
    } catch (err) {
      console.error('Failed to fetch messages:', err);
      set({ isLoadingMessages: false });
    }
  },

  sendMessage: async (conversationId: string, content: string) => {
    set({ isSendingMessage: true });
    try {
      const msg = await messagesApi.sendMessage(conversationId, content);
      get().receiveMessage(msg);
      set({ isSendingMessage: false });
      return msg;
    } catch (err) {
      set({ isSendingMessage: false });
      throw err;
    }
  },

  receiveMessage: (message: Message) => {
    const { activeConversationId } = get();
    const currentUserId = useAuthStore.getState().user?.id;
    const convId = message.conversationId;

    set((state) => {
      const currentList = state.messages[convId] || [];
      const exists = currentList.some((m) => m.id === message.id);
      const updatedList = exists
        ? currentList.map((m) => (m.id === message.id ? message : m))
        : [...currentList, message];

      // Always sort messages strictly chronological (oldest top, newest bottom)
      updatedList.sort((a, b) =>
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );

      let found = false;
      // Update conversation lastMessage & lastMessageAt
      const updatedConvs = state.conversations.map((c) => {
        if (c.id === convId) {
          found = true;
          const isCurrent = activeConversationId === convId;
          const isSender = message.senderId === currentUserId;
          return {
            ...c,
            lastMessage: message,
            lastMessageAt: message.createdAt,
            unreadCount: isCurrent || isSender ? 0 : (c.unreadCount || 0) + 1,
          };
        }
        return c;
      });

      // If this conversation was not found in local state (new chat initiated by someone else), fetch all conversations
      if (!found) {
        get().fetchConversations();
      }

      // Sort conversations by lastMessageAt descending
      updatedConvs.sort((a, b) => {
        const timeA = new Date(a.lastMessageAt || a.createdAt).getTime();
        const timeB = new Date(b.lastMessageAt || b.createdAt).getTime();
        return timeB - timeA;
      });

      return {
        messages: {
          ...state.messages,
          [convId]: updatedList,
        },
        conversations: updatedConvs,
      };
    });

    if (activeConversationId === convId && message.senderId !== currentUserId) {
      readReceiptsApi.markSeen(message.id).catch(() => {});
    }
  },

  updateMessageStatus: (messageId: string, status: MessageStatus) => {
    set((state) => {
      const newMessages = { ...state.messages };
      for (const [convId, msgList] of Object.entries(newMessages)) {
        const idx = msgList.findIndex((m) => m.id === messageId);
        if (idx !== -1) {
          const updated = [...msgList];
          updated[idx] = { ...updated[idx], status };
          newMessages[convId] = updated;
          break;
        }
      }
      return { messages: newMessages };
    });
  },

  markConversationAsRead: async (conversationId: string) => {
    try {
      await readReceiptsApi.markConversationSeen(conversationId);
      set((state) => ({
        conversations: state.conversations.map((c) =>
          c.id === conversationId ? { ...c, unreadCount: 0 } : c
        ),
      }));
    } catch (err) {
      console.warn('Failed to mark conversation read:', err);
    }
  },

  addConversation: (conv: Conversation) => {
    set((state) => {
      const exists = state.conversations.some((c) => c.id === conv.id);
      if (exists) return state;
      return {
        conversations: [conv, ...state.conversations],
      };
    });
  },

  updateConversation: (partialConv) => {
    set((state) => ({
      conversations: state.conversations.map((c) =>
        c.id === partialConv.id ? { ...c, ...partialConv } : c
      ),
    }));
  },

  setUserTyping: (conversationId, username, isTyping) => {
    set((state) => {
      const current = new Set(state.typingUsers[conversationId] || []);
      if (isTyping) {
        current.add(username);
      } else {
        current.delete(username);
      }
      return {
        typingUsers: {
          ...state.typingUsers,
          [conversationId]: current,
        },
      };
    });
  },
}));

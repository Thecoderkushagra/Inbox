import { apiClient } from './client';
import {
  AuthResponse,
  Conversation,
  ConversationParticipant,
  GlobalSearchResponse,
  LoginResponse,
  MediaAttachment,
  Message,
  NotificationResponse,
  PageResponse,
  PresenceResponse,
  ReadReceiptResponse,
  RegisterResponse,
  SearchConversation,
  SearchUser,
  User,
  UserProfile,
} from '@/types';

// Auth APIs
export const authApi = {
  login: (identifier: string, password: string): Promise<LoginResponse> =>
    apiClient<LoginResponse>('/api/v1/auth/login', {
      method: 'POST',
      body: JSON.stringify({ identifier, password }),
    }),

  register: (username: string, email: string, password: string, confirmPassword?: string): Promise<RegisterResponse> =>
    apiClient<RegisterResponse>('/api/v1/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password, confirmPassword: confirmPassword || password }),
    }),

  logout: (refreshToken: string): Promise<void> =>
    apiClient<void>('/api/v1/auth/logout', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    }),
};

// Users / Profile APIs
export const usersApi = {
  getMyProfile: (): Promise<UserProfile> =>
    apiClient<UserProfile>('/api/v1/users/me'),

  createMyProfile: (): Promise<UserProfile> =>
    apiClient<UserProfile>('/api/v1/users/me/profile', { method: 'POST' }),

  updateMyProfile: (data: Partial<UserProfile>): Promise<UserProfile> =>
    apiClient<UserProfile>('/api/v1/users/me', {
      method: 'PUT',
      body: JSON.stringify(data),
    }),

  getPublicProfile: (userId: string): Promise<UserProfile> =>
    apiClient<UserProfile>(`/api/v1/users/${userId}`),
};

// Conversations APIs
export const conversationsApi = {
  getUserConversations: (page = 0, size = 20): Promise<PageResponse<Conversation>> =>
    apiClient<PageResponse<Conversation>>(`/api/v1/conversations?page=${page}&size=${size}`),

  getConversation: (conversationId: string): Promise<Conversation> =>
    apiClient<Conversation>(`/api/v1/conversations/${conversationId}`),

  createPrivateConversation: (recipientId: string): Promise<Conversation> =>
    apiClient<Conversation>('/api/v1/conversations/private', {
      method: 'POST',
      body: JSON.stringify({ recipientId }),
    }),

  createGroupConversation: (name: string): Promise<Conversation> =>
    apiClient<Conversation>('/api/v1/conversations/group', {
      method: 'POST',
      body: JSON.stringify({ name }),
    }),

  getParticipants: (conversationId: string): Promise<ConversationParticipant[]> =>
    apiClient<ConversationParticipant[]>(`/api/v1/conversations/${conversationId}/participants`),

  addParticipant: (conversationId: string, userId: string): Promise<ConversationParticipant> =>
    apiClient<ConversationParticipant>(`/api/v1/conversations/${conversationId}/participants`, {
      method: 'POST',
      body: JSON.stringify({ userId }),
    }),

  removeParticipant: (conversationId: string, userId: string): Promise<void> =>
    apiClient<void>(`/api/v1/conversations/${conversationId}/participants/${userId}`, {
      method: 'DELETE',
    }),

  leaveConversation: (conversationId: string): Promise<void> =>
    apiClient<void>(`/api/v1/conversations/${conversationId}/leave`, {
      method: 'POST',
    }),

  updateConversation: (conversationId: string, name: string): Promise<Conversation> =>
    apiClient<Conversation>(`/api/v1/conversations/${conversationId}`, {
      method: 'PUT',
      body: JSON.stringify({ name }),
    }),

  archiveConversation: (conversationId: string): Promise<void> =>
    apiClient<void>(`/api/v1/conversations/${conversationId}/archive`, { method: 'POST' }),

  unarchiveConversation: (conversationId: string): Promise<void> =>
    apiClient<void>(`/api/v1/conversations/${conversationId}/unarchive`, { method: 'POST' }),
};

// Messages APIs
export const messagesApi = {
  getMessages: (conversationId: string, page = 0, size = 50): Promise<PageResponse<Message>> =>
    apiClient<PageResponse<Message>>(`/api/v1/conversations/${conversationId}/messages?page=${page}&size=${size}`),

  sendMessage: (conversationId: string, content: string): Promise<Message> =>
    apiClient<Message>(`/api/v1/conversations/${conversationId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content }),
    }),

  getMessage: (conversationId: string, messageId: string): Promise<Message> =>
    apiClient<Message>(`/api/v1/conversations/${conversationId}/messages/${messageId}`),

  updateMessage: (conversationId: string, messageId: string, content: string): Promise<Message> =>
    apiClient<Message>(`/api/v1/conversations/${conversationId}/messages/${messageId}`, {
      method: 'PUT',
      body: JSON.stringify({ content }),
    }),

  deleteMessage: (conversationId: string, messageId: string): Promise<void> =>
    apiClient<void>(`/api/v1/conversations/${conversationId}/messages/${messageId}`, {
      method: 'DELETE',
    }),
};

// Media APIs (Cloudinary)
export const mediaApi = {
  uploadAttachment: (messageId: string, file: File): Promise<MediaAttachment> => {
    const formData = new FormData();
    formData.append('file', file);
    return apiClient<MediaAttachment>(`/api/v1/media/messages/${messageId}`, {
      method: 'POST',
      body: formData,
    });
  },

  listAttachments: (messageId: string): Promise<MediaAttachment[]> =>
    apiClient<MediaAttachment[]>(`/api/v1/media/messages/${messageId}`),

  deleteAttachment: (storageKey: string): Promise<void> =>
    apiClient<void>(`/api/v1/media/${storageKey}`, {
      method: 'DELETE',
    }),
};

// Presence APIs
export const presenceApi = {
  getPresence: (userId: string): Promise<PresenceResponse> =>
    apiClient<PresenceResponse>(`/api/v1/presence/${userId}`),
};

// Read Receipts APIs
export const readReceiptsApi = {
  markDelivered: (messageId: string): Promise<void> =>
    apiClient<void>(`/api/v1/read-receipts/messages/${messageId}/delivered`, {
      method: 'PATCH',
    }),

  markSeen: (messageId: string): Promise<boolean> =>
    apiClient<boolean>(`/api/v1/read-receipts/messages/${messageId}/seen`, {
      method: 'PATCH',
    }),

  markConversationSeen: (conversationId: string): Promise<number> =>
    apiClient<number>(`/api/v1/read-receipts/conversations/${conversationId}/seen`, {
      method: 'PATCH',
    }),

  getReceipts: (messageId: string): Promise<ReadReceiptResponse[]> =>
    apiClient<ReadReceiptResponse[]>(`/api/v1/read-receipts/messages/${messageId}`),

  getUnreadCount: (): Promise<number> =>
    apiClient<number>('/api/v1/read-receipts/unread/count'),
};

// Notifications APIs
export const notificationsApi = {
  getNotifications: (): Promise<NotificationResponse[]> =>
    apiClient<NotificationResponse[]>('/api/v1/notifications'),

  getUnreadNotifications: (): Promise<NotificationResponse[]> =>
    apiClient<NotificationResponse[]>('/api/v1/notifications/unread'),

  getUnreadCount: (): Promise<number> =>
    apiClient<number>('/api/v1/notifications/unread/count'),

  markAsRead: (notificationId: string): Promise<NotificationResponse> =>
    apiClient<NotificationResponse>(`/api/v1/notifications/${notificationId}/read`, {
      method: 'PATCH',
    }),

  markAllAsRead: (): Promise<void> =>
    apiClient<void>('/api/v1/notifications/read-all', {
      method: 'PATCH',
    }),
};

// Search APIs
export const searchApi = {
  globalSearch: (keyword: string): Promise<GlobalSearchResponse> =>
    apiClient<GlobalSearchResponse>(`/api/v1/search/global?keyword=${encodeURIComponent(keyword)}`),

  searchUsers: (keyword: string, page = 0, size = 10): Promise<PageResponse<SearchUser>> =>
    apiClient<PageResponse<SearchUser>>(`/api/v1/search/users?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`),

  searchConversations: (keyword: string, page = 0, size = 10): Promise<PageResponse<SearchConversation>> =>
    apiClient<PageResponse<SearchConversation>>(`/api/v1/search/conversations?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`),
};

// Groups APIs
export const groupsApi = {
  createGroup: (title: string, description?: string, initialMemberIds?: string[]): Promise<Conversation> =>
    apiClient<Conversation>('/api/v1/groups', {
      method: 'POST',
      body: JSON.stringify({ title, description, initialMemberIds }),
    }),

  renameGroup: (groupId: string, title: string): Promise<Conversation> =>
    apiClient<Conversation>(`/api/v1/groups/${groupId}/rename`, {
      method: 'PATCH',
      body: JSON.stringify({ title }),
    }),

  addMember: (groupId: string, userId: string): Promise<ConversationParticipant> =>
    apiClient<ConversationParticipant>(`/api/v1/groups/${groupId}/members`, {
      method: 'POST',
      body: JSON.stringify({ userId }),
    }),

  removeMember: (groupId: string, userId: string): Promise<void> =>
    apiClient<void>(`/api/v1/groups/${groupId}/members/${userId}`, {
      method: 'DELETE',
    }),

  leaveGroup: (groupId: string): Promise<void> =>
    apiClient<void>(`/api/v1/groups/${groupId}/leave`, {
      method: 'POST',
    }),
};


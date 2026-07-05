export interface User {
  id: string;
  email: string;
  username: string;
  displayName?: string;
  avatarUrl?: string;
  status?: 'ONLINE' | 'OFFLINE' | 'AWAY' | 'BUSY';
  lastSeen?: string;
  createdAt: string;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  type: 'TEXT' | 'IMAGE' | 'VIDEO' | 'FILE';
  createdAt: string;
  readReceipts?: ReadReceipt[];
}

export interface Conversation {
  id: string;
  type: 'DIRECT' | 'GROUP';
  name?: string;
  participants: User[];
  lastMessage?: Message;
  unreadCount?: number;
  updatedAt: string;
}

export interface Friend {
  id: string;
  userId: string;
  friendId: string;
  status: 'PENDING' | 'ACCEPTED' | 'BLOCKED';
  createdAt: string;
}

export interface Notification {
  id: string;
  userId: string;
  type: 'MESSAGE' | 'FRIEND_REQUEST' | 'GROUP_INVITE' | 'SYSTEM';
  content: string;
  read: boolean;
  createdAt: string;
}

export interface MediaAttachment {
  id: string;
  url: string;
  type: 'IMAGE' | 'VIDEO' | 'FILE';
  size: number;
  name: string;
  mimeType: string;
}

export interface Group {
  id: string;
  name: string;
  description?: string;
  avatarUrl?: string;
  ownerId: string;
  members: User[];
  createdAt: string;
}

export interface ReadReceipt {
  userId: string;
  messageId: string;
  readAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface SocketMessage<T> {
  type: string;
  payload: T;
  timestamp: string;
}

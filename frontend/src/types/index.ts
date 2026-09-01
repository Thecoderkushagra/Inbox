export type RoleType = 'USER' | 'ADMIN';

export type PresenceStatus = 'ONLINE' | 'AWAY' | 'OFFLINE';

export type ConversationType = 'DIRECT' | 'GROUP';

export type MessageType = 'TEXT' | 'MEDIA' | 'SYSTEM';

export type MessageStatus = 'SENT' | 'DELIVERED' | 'SEEN';

export type ParticipantRole = 'OWNER' | 'ADMIN' | 'MEMBER';

export type ParticipantStatus = 'ACTIVE' | 'LEFT' | 'MUTED';

export type NotificationType =
  | 'FRIEND_REQUEST_RECEIVED'
  | 'FRIEND_REQUEST_ACCEPTED'
  | 'GROUP_MEMBER_ADDED'
  | 'GROUP_MEMBER_REMOVED'
  | 'GROUP_PROMOTED_TO_ADMIN'
  | 'GROUP_DEMOTED_FROM_ADMIN'
  | 'GROUP_RENAMED'
  | 'NEW_MESSAGE';

export interface User {
  id: string;
  email: string;
  username: string;
  roles: RoleType[];
  profile?: UserProfile;
}

export interface UserProfile {
  id?: string;
  userId: string;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
  bannerUrl?: string;
  location?: string;
  website?: string;
  birthDate?: string;
  gender?: string;
  profileVisibility?: 'PUBLIC' | 'PRIVATE' | 'FRIENDS_ONLY';
  verified?: boolean;
}

export interface LoginResponse {
  userId: string;
  username: string;
  email: string;
  roles: RoleType[];
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
}

export interface RegisterResponse {
  userId: string;
  username: string;
  email: string;
  status: string;
  createdAt: string;
  verificationRequired: boolean;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
}

export type AuthResponse = LoginResponse;

export interface ConversationParticipant {
  userId: string;
  username?: string;
  displayName?: string;
  avatarUrl?: string;
  role: ParticipantRole;
  status: ParticipantStatus;
  joinedAt: string;
}

export interface Conversation {
  id: string;
  type: ConversationType;
  title: string;
  description?: string;
  archived: boolean;
  lastMessageAt?: string;
  createdAt: string;
  participants: ConversationParticipant[];
  unreadCount?: number;
  lastMessage?: Message;
}

export interface MediaAttachment {
  attachmentId: string;
  storageKey: string;
  url: string;
  originalFilename: string;
  contentType: string;
  mediaType: string;
  fileSize: number;
  checksum?: string;
  createdAt: string;
}

export interface Message {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  messageType: MessageType;
  status: MessageStatus;
  edited: boolean;
  deleted: boolean;
  editedAt?: string;
  deletedAt?: string;
  createdAt: string;
  attachments?: MediaAttachment[];
}

export interface PresenceResponse {
  userId: string;
  status: PresenceStatus;
  lastSeen?: string;
}

export interface NotificationResponse {
  notificationId: string;
  type: NotificationType;
  title: string;
  message: string;
  referenceId?: string;
  read: boolean;
  readAt?: string;
  createdAt: string;
}

export interface ReadReceiptResponse {
  receiptId: string;
  messageId: string;
  userId: string;
  deliveredAt?: string;
  seenAt?: string;
  createdAt: string;
}

export interface SearchUser {
  userId: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
}

export interface SearchConversation {
  conversationId: string;
  title: string;
  description?: string;
  conversationType: ConversationType;
  updatedAt: string;
}

export interface GlobalSearchResponse {
  users: SearchUser[];
  groups: SearchConversation[];
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

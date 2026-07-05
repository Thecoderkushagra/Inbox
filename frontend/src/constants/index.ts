export const ApiEndpoints = {
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    ME: '/auth/me',
  },
  USER: {
    PROFILE: '/users/profile',
    UPDATE: '/users/profile',
    SEARCH: '/users/search',
  },
  CHAT: {
    CONVERSATIONS: '/conversations',
    MESSAGES: '/messages',
  },
  FRIENDS: {
    LIST: '/friends',
    REQUESTS: '/friends/requests',
  },
  GROUPS: {
    BASE: '/groups',
  },
  MEDIA: {
    UPLOAD: '/media/upload',
  },
  NOTIFICATIONS: {
    BASE: '/notifications',
  }
};

export const StorageKeys = {
  THEME: 'aurora_theme',
  ACCESS_TOKEN: 'aurora_access_token',
};

export const Routes = {
  LOGIN: '/login',
  REGISTER: '/register',
  VERIFY_OTP: '/verify-otp',
  FORGOT_PASSWORD: '/forgot-password',
  RESET_PASSWORD: '/reset-password',
  HOME: '/',
  CHAT: '/chat',
  PROFILE: '/profile',
  SETTINGS: '/settings',
  FRIENDS: '/friends',
  SEARCH: '/search',
  GROUPS: '/groups',
  NOTIFICATIONS: '/notifications',
};

export const Regex = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PASSWORD: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{8,}$/, // Min 8 chars, 1 letter, 1 number
};

export const Limits = {
  MAX_FILE_SIZE_MB: 10,
  PAGINATION_DEFAULT_SIZE: 20,
};

export const SocketDestinations = {
  TOPIC_USER: '/user/queue/messages',
  TOPIC_GROUP: '/topic/group/',
  APP_SEND: '/app/chat.sendMessage',
  APP_READ: '/app/chat.readReceipt',
};

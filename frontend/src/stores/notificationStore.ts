import { create } from 'zustand';
import { NotificationResponse } from '@/types';
import { notificationsApi } from '@/api';

interface NotificationState {
  notifications: NotificationResponse[];
  unreadCount: number;
  isLoading: boolean;

  fetchNotifications: () => Promise<void>;
  fetchUnreadCount: () => Promise<void>;
  addNotification: (notification: NotificationResponse) => void;
  markAsRead: (notificationId: string) => Promise<void>;
  markAllAsRead: () => Promise<void>;
}

export const useNotificationStore = create<NotificationState>((set) => ({
  notifications: [],
  unreadCount: 0,
  isLoading: false,

  fetchNotifications: async () => {
    set({ isLoading: true });
    try {
      const data = await notificationsApi.getNotifications();
      set({ notifications: data, isLoading: false });
    } catch (err) {
      console.error('Failed to fetch notifications:', err);
      set({ isLoading: false });
    }
  },

  fetchUnreadCount: async () => {
    try {
      const count = await notificationsApi.getUnreadCount();
      set({ unreadCount: count });
    } catch (err) {
      console.warn('Failed to fetch unread count:', err);
    }
  },

  addNotification: (notification) => {
    set((state) => ({
      notifications: [notification, ...state.notifications],
      unreadCount: state.unreadCount + 1,
    }));
  },

  markAsRead: async (notificationId) => {
    try {
      await notificationsApi.markAsRead(notificationId);
      set((state) => ({
        notifications: state.notifications.map((n) =>
          n.notificationId === notificationId ? { ...n, read: true } : n
        ),
        unreadCount: Math.max(0, state.unreadCount - 1),
      }));
    } catch (err) {
      console.error('Failed to mark notification read:', err);
    }
  },

  markAllAsRead: async () => {
    try {
      await notificationsApi.markAllAsRead();
      set((state) => ({
        notifications: state.notifications.map((n) => ({ ...n, read: true })),
        unreadCount: 0,
      }));
    } catch (err) {
      console.error('Failed to mark all notifications read:', err);
    }
  },
}));

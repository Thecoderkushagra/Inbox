import { create } from 'zustand';
import { PresenceResponse, PresenceStatus } from '@/types';
import { presenceApi } from '@/api';

interface PresenceState {
  presenceMap: Record<string, PresenceResponse>; // userId -> PresenceResponse
  getUserPresence: (userId: string) => PresenceResponse | undefined;
  setPresence: (userId: string, status: PresenceStatus, lastSeen?: string) => void;
  fetchUserPresence: (userId: string) => Promise<void>;
}

export const usePresenceStore = create<PresenceState>((set, get) => ({
  presenceMap: {},

  getUserPresence: (userId: string) => {
    return get().presenceMap[userId];
  },

  setPresence: (userId: string, status: PresenceStatus, lastSeen?: string) => {
    set((state) => ({
      presenceMap: {
        ...state.presenceMap,
        [userId]: { userId, status, lastSeen },
      },
    }));
  },

  fetchUserPresence: async (userId: string) => {
    try {
      const data = await presenceApi.getPresence(userId);
      set((state) => ({
        presenceMap: {
          ...state.presenceMap,
          [userId]: data,
        },
      }));
    } catch (err) {
      console.warn('Failed to fetch user presence:', err);
    }
  },
}));

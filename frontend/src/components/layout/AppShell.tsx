import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { NavigationSidebar } from '@/components/navigation/NavigationSidebar';
import { BottomNavigationBar } from '@/components/navigation/BottomNavigationBar';
import { useChatStore } from '@/stores/chatStore';
import { useNotificationStore } from '@/stores/notificationStore';
import { useWebSocket } from '@/context/WebSocketContext';

export const AppShell: React.FC = () => {
  const { fetchConversations } = useChatStore();
  const { fetchNotifications } = useNotificationStore();
  const { isConnected } = useWebSocket();

  useEffect(() => {
    fetchConversations();
    fetchNotifications();
  }, [fetchConversations, fetchNotifications]);

  return (
    <div className="h-screen w-screen flex bg-slate-950 text-slate-100 overflow-hidden select-none">
      {/* Desktop Left Navigation Rail */}
      <NavigationSidebar />

      {/* Main Content Area */}
      <main className="flex-1 flex flex-col h-full overflow-hidden relative">
        <Outlet />
      </main>

      {/* Mobile Bottom Navigation Bar */}
      <BottomNavigationBar />
    </div>
  );
};

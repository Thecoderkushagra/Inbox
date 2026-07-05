import { useState, useMemo } from 'react';
import { Outlet, useLocation } from 'react-router-dom';
import { Sidebar } from '../components/layout/Sidebar';
import { TopBar } from '../components/layout/TopBar';
import { Routes } from '../constants';

const pageTitles: Record<string, string> = {
  [Routes.CHAT]: 'Chats',
  [Routes.FRIENDS]: 'Friends',
  [Routes.GROUPS]: 'Groups',
  [Routes.SEARCH]: 'Search',
  [Routes.NOTIFICATIONS]: 'Notifications',
  [Routes.PROFILE]: 'Profile',
  [Routes.SETTINGS]: 'Settings',
};

export function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const location = useLocation();

  const pageTitle = useMemo(() => {
    return pageTitles[location.pathname] || 'Aurora Stream';
  }, [location.pathname]);

  return (
    <div className="flex h-screen overflow-hidden bg-bg">
      {/* Sidebar */}
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      {/* Main Content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title={pageTitle} onMenuClick={() => setSidebarOpen(true)} />
        <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

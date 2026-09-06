import React from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { useNotificationStore } from '@/stores/notificationStore';
import { useWebSocket } from '@/context/WebSocketContext';
import { Avatar } from '@/components/ui/Avatar';
import { NotificationDropdown } from '@/components/notifications/NotificationDropdown';
import { cn } from '@/utils/cn';
import inboxLogo from '@/assets/inbox-logo.png';
import {
  MessageSquare,
  Users,
  UserPlus,
  Settings,
  LogOut,
} from 'lucide-react';

export const NavigationSidebar: React.FC = () => {
  const { user, profile, logout } = useAuthStore();
  const { conversations } = useChatStore();
  const { unreadCount: unreadNotifications } = useNotificationStore();
  const { isConnected } = useWebSocket();
  const navigate = useNavigate();
  const location = useLocation();

  // Total unread messages count
  const totalUnreadMessages = conversations.reduce(
    (acc, c) => acc + (c.unreadCount || 0),
    0
  );

  const navItems = [
    {
      to: '/inbox',
      icon: MessageSquare,
      label: 'Chat',
      badge: totalUnreadMessages > 0 ? totalUnreadMessages : undefined,
    },
    {
      to: '/people',
      icon: Users,
      label: 'Search Peoples',
    },
    {
      to: '/groups/create',
      icon: UserPlus,
      label: 'Create Groups',
    },
    {
      to: '/settings',
      icon: Settings,
      label: 'Settings',
    },
  ];

  const handleLogout = async () => {
    if (confirm('Are you sure you want to sign out?')) {
      await logout();
      navigate('/login');
    }
  };

  return (
    <aside
      className="hidden md:flex md:w-64 h-full flex-col bg-slate-950 border-r border-slate-800/80 flex-shrink-0 select-none z-30 transition-all duration-200"
      aria-label="Sidebar Navigation"
    >
      {/* Brand Header */}
      <div className="h-16 px-4 flex items-center justify-center md:justify-start gap-3 border-b border-slate-800/80">
        <div className="w-10 h-10 rounded-2xl bg-indigo-600/15 border border-indigo-500/30 flex items-center justify-center shadow-lg shadow-indigo-500/10 flex-shrink-0">
          <img
            src={inboxLogo}
            alt="Inbox Logo"
            className="w-7 h-7 object-contain rounded-xl"
          />
        </div>
        <div className="hidden md:block min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <h1 className="text-base font-extrabold text-white tracking-tight leading-none">
              Inbox
            </h1>
            <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-300 font-semibold">
              E2EE
            </span>
          </div>
          <div className="flex items-center gap-1.5 mt-1">
            <span
              className={cn(
                'w-2 h-2 rounded-full transition-colors',
                isConnected
                  ? 'bg-emerald-500 shadow-sm shadow-emerald-500/50 animate-pulse'
                  : 'bg-rose-500'
              )}
            />
            <span className="text-[10px] text-slate-400 font-medium">
              {isConnected ? 'Real-time connected' : 'Reconnecting...'}
            </span>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-3 py-4 space-y-2 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive =
            item.to === '/inbox'
              ? location.pathname.startsWith('/inbox') || location.pathname === '/'
              : location.pathname.startsWith(item.to);

          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={cn(
                'flex items-center justify-center md:justify-between px-3 py-2.5 rounded-2xl text-xs font-semibold transition-all duration-150 group relative',
                isActive
                  ? 'bg-indigo-600/15 text-indigo-300 border border-indigo-500/30 shadow-sm'
                  : 'text-slate-400 hover:text-slate-100 hover:bg-slate-900 border border-transparent'
              )}
              title={item.label}
            >
              <div className="flex items-center gap-3">
                <Icon
                  className={cn(
                    'w-4 h-4 transition-colors',
                    isActive ? 'text-indigo-400' : 'text-slate-400 group-hover:text-slate-200'
                  )}
                />
                <span className="hidden md:inline">{item.label}</span>
              </div>

              {item.badge !== undefined && item.badge > 0 && (
                <span className="hidden md:inline-flex px-2 py-0.5 text-[10px] font-bold rounded-full bg-indigo-600 text-white shadow-sm">
                  {item.badge}
                </span>
              )}

              {/* Mobile badge dot */}
              {item.badge !== undefined && item.badge > 0 && (
                <span className="md:hidden absolute top-2 right-2 w-2 h-2 rounded-full bg-indigo-500" />
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Notifications & User Footer Profile */}
      <div className="p-3 border-t border-slate-800/80 space-y-2 bg-slate-950/80">
        <div className="hidden md:flex items-center justify-between px-2 text-xs text-slate-400">
          <span>Notifications</span>
          <NotificationDropdown />
        </div>

        <div className="flex items-center justify-between p-2 rounded-2xl bg-slate-900/90 border border-slate-800/80">
          <div
            onClick={() => navigate('/settings')}
            className="flex items-center gap-2.5 min-w-0 cursor-pointer flex-1"
            title="View Profile Settings"
          >
            <Avatar
              name={profile?.displayName || user?.username || 'User'}
              src={profile?.avatarUrl}
              size="sm"
              status="ONLINE"
            />
            <div className="hidden md:block min-w-0 text-left">
              <div className="text-xs font-semibold text-white truncate">
                {profile?.displayName || user?.username}
              </div>
              <div className="text-[10px] text-slate-400 truncate">@{user?.username}</div>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="hidden md:flex p-1.5 text-slate-400 hover:text-rose-400 hover:bg-slate-800 rounded-xl transition-colors cursor-pointer"
            title="Sign Out"
            aria-label="Sign Out"
          >
            <LogOut className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </aside>
  );
};

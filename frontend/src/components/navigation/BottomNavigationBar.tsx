import React from 'react';
import { NavLink, useLocation, useNavigate } from 'react-router-dom';
import { useChatStore } from '@/stores/chatStore';
import { cn } from '@/utils/cn';
import { MessageSquare, Users, UserPlus, Settings } from 'lucide-react';

export const BottomNavigationBar: React.FC = () => {
  const { conversations } = useChatStore();
  const location = useLocation();

  const totalUnreadMessages = conversations.reduce(
    (acc, c) => acc + (c.unreadCount || 0),
    0
  );

  const isInboxActive =
    location.pathname.startsWith('/inbox') || location.pathname === '/';

  const isInsideChat =
    location.pathname.startsWith('/inbox/') && location.pathname !== '/inbox';

  if (isInsideChat) {
    return null;
  }

  return (
    <nav
      className="md:hidden h-16 bg-slate-950/95 backdrop-blur-xl border-t border-slate-800/80 flex items-center justify-around px-2 z-40 fixed bottom-0 left-0 right-0 select-none pb-safe"
      aria-label="Mobile Navigation Bar"
    >
      {/* 1. Chat */}
      <NavLink
        to="/inbox"
        className={cn(
          'flex flex-col items-center justify-center flex-1 py-1 text-[10px] font-medium transition-colors relative',
          isInboxActive ? 'text-indigo-400 font-semibold' : 'text-slate-400 hover:text-slate-200'
        )}
      >
        <div className="relative">
          <MessageSquare className="w-5 h-5 mb-0.5" />
          {totalUnreadMessages > 0 && (
            <span className="absolute -top-1 -right-2 px-1.5 py-0.2 min-w-[16px] text-[9px] font-bold rounded-full bg-indigo-600 text-white text-center">
              {totalUnreadMessages}
            </span>
          )}
        </div>
        <span>Chat</span>
      </NavLink>

      {/* 2. Search Peoples */}
      <NavLink
        to="/people"
        className={({ isActive }) =>
          cn(
            'flex flex-col items-center justify-center flex-1 py-1 text-[10px] font-medium transition-colors',
            isActive ? 'text-indigo-400 font-semibold' : 'text-slate-400 hover:text-slate-200'
          )
        }
      >
        <Users className="w-5 h-5 mb-0.5" />
        <span>People</span>
      </NavLink>

      {/* 3. Create Group */}
      <NavLink
        to="/groups/create"
        className={({ isActive }) =>
          cn(
            'flex flex-col items-center justify-center flex-1 py-1 text-[10px] font-medium transition-colors',
            isActive ? 'text-indigo-400 font-semibold' : 'text-slate-400 hover:text-slate-200'
          )
        }
      >
        <UserPlus className="w-5 h-5 mb-0.5" />
        <span>New Group</span>
      </NavLink>

      {/* 4. Settings */}
      <NavLink
        to="/settings"
        className={({ isActive }) =>
          cn(
            'flex flex-col items-center justify-center flex-1 py-1 text-[10px] font-medium transition-colors',
            isActive ? 'text-indigo-400 font-semibold' : 'text-slate-400 hover:text-slate-200'
          )
        }
      >
        <Settings className="w-5 h-5 mb-0.5" />
        <span>Settings</span>
      </NavLink>
    </nav>
  );
};

import React, { useEffect, useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { useWebSocket } from '@/context/WebSocketContext';
import { ConversationList } from '@/components/chat/ConversationList';
import { ChatWindow } from '@/components/chat/ChatWindow';
import { GlobalSearchBar } from '@/components/search/GlobalSearchBar';
import { NotificationDropdown } from '@/components/notifications/NotificationDropdown';
import { ProfileSettingsModal } from '@/components/profile/ProfileSettingsModal';
import { Avatar } from '@/components/ui/Avatar';
import { LogOut, Settings, Radio } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const ChatPage: React.FC = () => {
  const { user, profile, logout } = useAuthStore();
  const { fetchConversations } = useChatStore();
  const { isConnected } = useWebSocket();
  const navigate = useNavigate();

  const [showProfileModal, setShowProfileModal] = useState(false);

  useEffect(() => {
    fetchConversations();
  }, []);

  const handleLogout = async () => {
    if (confirm('Are you sure you want to sign out?')) {
      await logout();
      navigate('/login');
    }
  };

  return (
    <div className="h-screen w-screen flex flex-col bg-slate-950 text-slate-100 overflow-hidden select-none">
      {/* Global Top Navigation Bar */}
      <header className="h-16 px-4 sm:px-6 bg-slate-900/90 backdrop-blur-md border-b border-slate-800/80 flex items-center justify-between z-20 flex-shrink-0">
        {/* Brand */}
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-2xl bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center shadow-md">
            <img src="/frontend/src/assets/inbox-logo.png" alt="Inbox" className="w-6 h-6 object-contain" />
          </div>
          <div>
            <h1 className="text-base font-bold text-white tracking-tight leading-none">Inbox</h1>
            <div className="flex items-center gap-1.5 mt-0.5">
              <span
                className={`w-2 h-2 rounded-full ${
                  isConnected ? 'bg-emerald-500 shadow-sm shadow-emerald-500/50 animate-pulse' : 'bg-rose-500'
                }`}
              />
              <span className="text-[10px] text-slate-400 font-medium">
                {isConnected ? 'Connected' : 'Connecting...'}
              </span>
            </div>
          </div>
        </div>

        {/* Global Search Bar */}
        <div className="hidden sm:block">
          <GlobalSearchBar />
        </div>

        {/* Right Actions */}
        <div className="flex items-center gap-2">
          <NotificationDropdown />

          <button
            onClick={() => setShowProfileModal(true)}
            className="p-2 rounded-xl text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
            title="Profile Settings"
          >
            <Settings className="w-5 h-5" />
          </button>

          <button
            onClick={handleLogout}
            className="p-2 rounded-xl text-slate-400 hover:text-rose-400 hover:bg-slate-800 transition-colors"
            title="Sign Out"
          >
            <LogOut className="w-5 h-5" />
          </button>

          {/* Current User Avatar */}
          <div
            onClick={() => setShowProfileModal(true)}
            className="cursor-pointer pl-2 ml-1 border-l border-slate-800 flex items-center gap-2.5"
          >
            <Avatar
              name={profile?.displayName || user?.username || 'User'}
              src={profile?.avatarUrl}
              size="sm"
              status="ONLINE"
            />
            <div className="hidden md:block text-left">
              <div className="text-xs font-semibold text-white leading-none">
                {profile?.displayName || user?.username}
              </div>
              <div className="text-[10px] text-slate-400 mt-0.5">@{user?.username}</div>
            </div>
          </div>
        </div>
      </header>

      {/* Main Messaging Dashboard */}
      <main className="flex-1 flex overflow-hidden relative">
        <ConversationList />
        <ChatWindow />
      </main>

      {/* Profile Settings Modal */}
      {showProfileModal && (
        <ProfileSettingsModal
          isOpen={showProfileModal}
          onClose={() => setShowProfileModal(false)}
        />
      )}
    </div>
  );
};

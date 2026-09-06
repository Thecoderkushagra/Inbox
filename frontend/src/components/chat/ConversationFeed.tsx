import React, { useState, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChatStore } from '@/stores/chatStore';
import { ConversationRow } from './ConversationRow';
import { Skeleton } from '@/components/ui/Skeleton';
import { EmptyState } from '@/components/ui/EmptyState';
import { Search, Users, X, MessageSquare, Filter } from 'lucide-react';
import { cn } from '@/utils/cn';

type FilterTab = 'all' | 'unread' | 'direct' | 'groups';

export const ConversationFeed: React.FC = () => {
  const { conversations, isLoadingConversations } = useChatStore();
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<FilterTab>('all');

  const filteredConversations = useMemo(() => {
    return conversations.filter((c) => {
      // Tab filter
      if (activeTab === 'unread' && (!c.unreadCount || c.unreadCount === 0)) {
        return false;
      }
      if (activeTab === 'direct' && c.type !== 'DIRECT') {
        return false;
      }
      if (activeTab === 'groups' && c.type !== 'GROUP') {
        return false;
      }

      // Search query filter
      if (searchQuery.trim()) {
        const q = searchQuery.toLowerCase();
        const titleMatch = c.title?.toLowerCase().includes(q);
        const lastMsgMatch = c.lastMessage?.content?.toLowerCase().includes(q);
        const participantMatch = c.participants?.some(
          (p) =>
            p.displayName?.toLowerCase().includes(q) ||
            p.username?.toLowerCase().includes(q)
        );
        return titleMatch || lastMsgMatch || participantMatch;
      }

      return true;
    });
  }, [conversations, activeTab, searchQuery]);

  return (
    <div className="w-full md:w-88 lg:w-96 h-full flex flex-col bg-slate-900/60 border-r border-slate-800/80 select-none relative flex-shrink-0">
      {/* Top Header & Search */}
      <div className="p-4 border-b border-slate-800/80 space-y-3 bg-slate-950/40">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-extrabold text-white tracking-tight">Messages</h2>
            <span className="text-[11px] font-bold px-2 py-0.5 rounded-full bg-slate-800 text-slate-300">
              {conversations.length}
            </span>
          </div>

          <button
            onClick={() => navigate('/people')}
            className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700/60 active:scale-95 transition-all cursor-pointer"
            title="Find people to chat"
            aria-label="Find people"
          >
            <Users className="w-4 h-4" />
          </button>
        </div>

        {/* Search input with keyboard hint */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Filter conversations..."
            className="w-full bg-slate-950 text-xs text-slate-100 placeholder-slate-500 border border-slate-800 focus:border-indigo-500/80 rounded-xl pl-9 pr-8 py-2.5 outline-none transition-all"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 p-0.5 text-slate-500 hover:text-slate-300 rounded"
              aria-label="Clear filter"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-1.5 pt-0.5 overflow-x-auto no-scrollbar">
          {(
            [
              { id: 'all', label: 'All' },
              { id: 'unread', label: 'Unread' },
              { id: 'direct', label: 'Direct' },
              { id: 'groups', label: 'Groups' },
            ] as const
          ).map((tab) => {
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={cn(
                  'px-3 py-1 text-xs font-semibold rounded-full transition-all cursor-pointer whitespace-nowrap',
                  isActive
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'bg-slate-950/80 text-slate-400 hover:text-slate-200 border border-slate-800/80'
                )}
              >
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Feed Conversation List */}
      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {isLoadingConversations ? (
          <div className="p-3 space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex items-center gap-3 p-2">
                <Skeleton className="w-10 h-10 rounded-full" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-3.5 w-3/4 rounded" />
                  <Skeleton className="h-3 w-1/2 rounded" />
                </div>
              </div>
            ))}
          </div>
        ) : filteredConversations.length === 0 ? (
          <EmptyState
            icon={<MessageSquare className="w-6 h-6" />}
            title="No conversations"
            description={
              searchQuery
                ? `No messages matched "${searchQuery}"`
                : activeTab !== 'all'
                ? `No ${activeTab} conversations found.`
                : 'Your inbox is empty. Start a conversation with a teammate or friend.'
            }
            actionLabel={!searchQuery && activeTab === 'all' ? 'Start a Chat' : undefined}
            onAction={() => navigate('/compose')}
            className="my-8"
          />
        ) : (
          filteredConversations.map((conv) => (
            <ConversationRow key={conv.id} conversation={conv} />
          ))
        )}
      </div>
    </div>
  );
};

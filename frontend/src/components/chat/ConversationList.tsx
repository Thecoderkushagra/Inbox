import React, { useState } from 'react';
import { Conversation, User } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { formatConversationTime } from '@/utils/formatDate';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { usePresenceStore } from '@/stores/presenceStore';
import { cn } from '@/utils/cn';
import { Search, Plus, MessageSquare, Users } from 'lucide-react';
import { NewChatModal } from './NewChatModal';

export const ConversationList: React.FC = () => {
  const { user } = useAuthStore();
  const {
    conversations,
    activeConversationId,
    setActiveConversationId,
    isLoadingConversations,
  } = useChatStore();
  const { getUserPresence } = usePresenceStore();

  const [filterQuery, setFilterQuery] = useState('');
  const [showNewChatModal, setShowNewChatModal] = useState(false);

  const filteredConversations = conversations.filter((c) =>
    (c.title || '').toLowerCase().includes(filterQuery.toLowerCase())
  );

  return (
    <div className="w-80 md:w-96 h-full flex flex-col bg-slate-900/60 border-r border-slate-800/80 select-none">
      {/* Search and New Chat Header */}
      <div className="p-4 border-b border-slate-800/80 space-y-3">
        <div className="flex items-center justify-between">
          <h2 className="text-base font-bold text-white tracking-tight flex items-center gap-2">
            <span>Chats</span>
            <span className="text-xs px-2 py-0.5 rounded-full bg-slate-800 text-slate-400 font-normal">
              {conversations.length}
            </span>
          </h2>
          <button
            onClick={() => setShowNewChatModal(true)}
            className="p-2 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white shadow-md shadow-indigo-600/20 active:scale-95 transition-all cursor-pointer"
            title="Start new conversation"
          >
            <Plus className="w-4 h-4" />
          </button>
        </div>

        {/* Local Conversation Filter */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={filterQuery}
            onChange={(e) => setFilterQuery(e.target.value)}
            placeholder="Filter conversations..."
            className="w-full bg-slate-950/80 text-xs text-slate-100 placeholder-slate-500 border border-slate-800 rounded-xl pl-9 pr-3.5 py-2 focus:outline-none focus:border-indigo-500/80 transition-colors"
          />
        </div>
      </div>

      {/* Conversation Item List */}
      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {isLoadingConversations ? (
          <div className="p-6 text-center text-xs text-slate-500">Loading chats...</div>
        ) : filteredConversations.length === 0 ? (
          <div className="p-8 text-center text-xs text-slate-500 space-y-2">
            <MessageSquare className="w-8 h-8 mx-auto text-slate-600" />
            <p>No conversations found</p>
            <button
              onClick={() => setShowNewChatModal(true)}
              className="text-indigo-400 hover:underline font-medium"
            >
              Start a new chat
            </button>
          </div>
        ) : (
          filteredConversations.map((conv) => {
            const isSelected = conv.id === activeConversationId;
            const otherParticipant = conv.participants?.find((p) => p.userId !== user?.id);
            const otherPresence = otherParticipant ? getUserPresence(otherParticipant.userId) : undefined;

            return (
              <div
                key={conv.id}
                onClick={() => setActiveConversationId(conv.id)}
                className={cn(
                  'flex items-center gap-3 p-3 rounded-2xl cursor-pointer transition-all duration-150 relative group',
                  isSelected
                    ? 'bg-indigo-600/15 border border-indigo-500/30'
                    : 'hover:bg-slate-800/50 border border-transparent'
                )}
              >
                <Avatar
                  name={conv.title || 'Chat'}
                  size="md"
                  status={conv.type === 'DIRECT' ? otherPresence?.status : undefined}
                />

                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-0.5">
                    <h4
                      className={cn(
                        'text-sm font-semibold truncate flex items-center gap-1.5',
                        isSelected ? 'text-indigo-300' : 'text-slate-200'
                      )}
                    >
                      <span>{conv.title || 'Conversation'}</span>
                      {conv.type === 'GROUP' && (
                        <Users className="w-3 h-3 text-slate-400 flex-shrink-0" />
                      )}
                    </h4>
                    <span className="text-[10px] text-slate-500 flex-shrink-0">
                      {formatConversationTime(conv.lastMessageAt || conv.createdAt)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <p className="text-xs text-slate-400 truncate pr-2">
                      {conv.lastMessage
                        ? conv.lastMessage.deleted
                          ? 'Message deleted'
                          : conv.lastMessage.content
                        : 'No messages yet'}
                    </p>

                    {conv.unreadCount && conv.unreadCount > 0 ? (
                      <Badge variant="primary" className="rounded-full px-1.5 py-0.2 text-[10px]">
                        {conv.unreadCount}
                      </Badge>
                    ) : null}
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>

      {showNewChatModal && (
        <NewChatModal
          isOpen={showNewChatModal}
          onClose={() => setShowNewChatModal(false)}
        />
      )}
    </div>
  );
};

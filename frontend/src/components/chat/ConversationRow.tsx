import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Conversation } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { formatConversationTime } from '@/utils/formatDate';
import { useAuthStore } from '@/stores/authStore';
import { usePresenceStore } from '@/stores/presenceStore';
import { useChatStore } from '@/stores/chatStore';
import { cn } from '@/utils/cn';
import { Users, Check, CheckCheck } from 'lucide-react';

interface ConversationRowProps {
  conversation: Conversation;
}

export const ConversationRow: React.FC<ConversationRowProps> = ({ conversation }) => {
  const { user } = useAuthStore();
  const { getUserPresence } = usePresenceStore();
  const { typingUsers } = useChatStore();
  const navigate = useNavigate();
  const { conversationId: activeId } = useParams<{ conversationId?: string }>();

  const isSelected = activeId === conversation.id;
  const isUnread = !!conversation.unreadCount && conversation.unreadCount > 0;

  // For 1-on-1 conversations, extract the other participant
  const otherParticipant = conversation.participants?.find((p) => p.userId !== user?.id);
  const otherPresence = otherParticipant ? getUserPresence(otherParticipant.userId) : undefined;

  const getDisplayTitle = () => {
    if (conversation.type === 'DIRECT') {
      if (otherParticipant?.displayName) return otherParticipant.displayName;
      if (otherParticipant?.username) return otherParticipant.username;
    }
    return conversation.title || 'Conversation';
  };

  const displayTitle = getDisplayTitle();

  const isTyping =
    typingUsers[conversation.id] && typingUsers[conversation.id].size > 0;

  const handleClick = () => {
    navigate(`/inbox/${conversation.id}`);
  };

  return (
    <div
      onClick={handleClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          handleClick();
        }
      }}
      className={cn(
        'group flex items-center gap-3 p-3 rounded-2xl cursor-pointer transition-all duration-150 relative select-none border focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500',
        isSelected
          ? 'bg-indigo-600/15 border-indigo-500/30 text-white shadow-sm'
          : isUnread
          ? 'bg-slate-900/90 border-slate-800/80 hover:bg-slate-800/60'
          : 'bg-transparent border-transparent hover:bg-slate-900/60 text-slate-300'
      )}
    >
      {/* Unread active indicator strip */}
      {isUnread && !isSelected && (
        <span className="absolute left-1 top-3 bottom-3 w-1 bg-indigo-500 rounded-full" />
      )}

      <Avatar
        name={displayTitle}
        src={otherParticipant?.avatarUrl}
        size="md"
        status={conversation.type === 'DIRECT' ? otherPresence?.status : undefined}
      />

      <div className="flex-1 min-w-0">
        <div className="flex items-center justify-between mb-0.5">
          <div className="flex items-center gap-1.5 min-w-0 pr-1">
            <span
              className={cn(
                'text-xs font-semibold truncate',
                isSelected
                  ? 'text-indigo-200 font-bold'
                  : isUnread
                  ? 'text-white font-bold'
                  : 'text-slate-200'
              )}
            >
              {displayTitle}
            </span>
            {conversation.type === 'GROUP' && (
              <Users className="w-3.5 h-3.5 text-slate-400 flex-shrink-0" />
            )}
          </div>

          <span className="text-[10px] text-slate-500 font-medium flex-shrink-0">
            {formatConversationTime(conversation.lastMessageAt || conversation.createdAt)}
          </span>
        </div>

        <div className="flex items-center justify-between gap-2">
          {isTyping ? (
            <span className="text-xs text-indigo-400 font-medium italic animate-pulse truncate">
              typing...
            </span>
          ) : (
            <p
              className={cn(
                'text-xs truncate leading-snug',
                isUnread
                  ? 'text-slate-100 font-semibold'
                  : 'text-slate-400 group-hover:text-slate-300'
              )}
            >
              {conversation.lastMessage
                ? conversation.lastMessage.deleted
                  ? 'Message deleted'
                  : conversation.lastMessage.content
                : 'No messages yet'}
            </p>
          )}

          {isUnread && (
            <Badge
              variant="primary"
              className="rounded-full px-1.5 py-0.2 text-[10px] font-bold shadow-sm shadow-indigo-600/30 flex-shrink-0"
            >
              {conversation.unreadCount}
            </Badge>
          )}
        </div>
      </div>
    </div>
  );
};

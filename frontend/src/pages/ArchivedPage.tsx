import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useChatStore } from '@/stores/chatStore';
import { useAuthStore } from '@/stores/authStore';
import { conversationsApi } from '@/api';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { EmptyState } from '@/components/ui/EmptyState';
import { MobileHeader } from '@/components/navigation/MobileHeader';
import { Archive, ArchiveRestore, MessageSquare, ArrowRight } from 'lucide-react';
import { formatConversationTime } from '@/utils/formatDate';
import toast from 'react-hot-toast';

export const ArchivedPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { conversations, fetchConversations, setActiveConversationId } = useChatStore();

  const archivedConversations = conversations.filter((c) => c.archived);

  const handleUnarchive = async (e: React.MouseEvent, conversationId: string) => {
    e.stopPropagation();
    try {
      await conversationsApi.unarchiveConversation(conversationId);
      await fetchConversations();
      toast.success('Conversation unarchived');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to unarchive';
      toast.error(msg);
    }
  };

  const handleOpen = (conversationId: string) => {
    setActiveConversationId(conversationId);
    navigate(`/inbox/${conversationId}`);
  };

  return (
    <div className="h-full w-full flex flex-col bg-slate-950 text-slate-100 overflow-hidden pb-16 md:pb-0">
      <MobileHeader title="Archived Chats" showBack={true} backTo="/inbox" />

      <div className="flex-1 overflow-y-auto p-4 sm:p-8 max-w-4xl mx-auto w-full space-y-6">
        {/* Desktop Header */}
        <div className="hidden md:block">
          <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <span>Archived Conversations</span>
            <Archive className="w-5 h-5 text-indigo-400" />
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Archived chats are hidden from your main inbox until new messages arrive.
          </p>
        </div>

        {archivedConversations.length === 0 ? (
          <EmptyState
            icon={<Archive className="w-8 h-8" />}
            title="No archived conversations"
            description="When you archive conversations you want to store away, they will appear here."
            className="my-16"
          />
        ) : (
          <div className="space-y-2.5">
            {archivedConversations.map((conv) => {
              const otherParticipant = conv.participants?.find((p) => p.userId !== user?.id);
              const title =
                conv.type === 'DIRECT'
                  ? otherParticipant?.displayName || otherParticipant?.username || 'User'
                  : conv.title || 'Group';

              return (
                <div
                  key={conv.id}
                  onClick={() => handleOpen(conv.id)}
                  className="p-4 rounded-2xl bg-slate-900/60 hover:bg-slate-900 border border-slate-800 flex items-center justify-between gap-4 transition-all cursor-pointer group shadow-sm"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <Avatar name={title} src={otherParticipant?.avatarUrl} size="md" />
                    <div className="min-w-0">
                      <div className="text-sm font-bold text-white group-hover:text-indigo-300 truncate transition-colors">
                        {title}
                      </div>
                      <div className="text-xs text-slate-400 truncate">
                        {conv.lastMessage?.content || 'No messages'}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 flex-shrink-0">
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={(e) => handleUnarchive(e, conv.id)}
                      className="rounded-xl gap-1.5"
                      title="Unarchive"
                    >
                      <ArchiveRestore className="w-3.5 h-3.5" />
                      <span className="hidden sm:inline">Unarchive</span>
                    </Button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

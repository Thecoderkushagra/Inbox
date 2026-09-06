import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { Conversation, User } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { UserProfileModal } from '@/components/profile/UserProfileModal';
import { AddMemberModal } from './AddMemberModal';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { apiClient } from '@/api/client';
import { searchApi, conversationsApi } from '@/api';
import {
  X,
  Shield,
  UserPlus,
  UserMinus,
  LogOut,
  Edit2,
  Check,
  Users,
  Info,
  Calendar,
  MessageSquare,
  User as UserIcon,
} from 'lucide-react';
import { formatConversationTime } from '@/utils/formatDate';

interface ConversationInspectorProps {
  conversation: Conversation;
  onClose: () => void;
}

export const ConversationInspector: React.FC<ConversationInspectorProps> = ({
  conversation,
  onClose,
}) => {
  const { user } = useAuthStore();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [newTitle, setNewTitle] = useState(conversation.title || '');
  const [newMemberInput, setNewMemberInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isAddMemberOpen, setIsAddMemberOpen] = useState(false);
  const [selectedParticipantForProfile, setSelectedParticipantForProfile] = useState<{
    userId: string;
    username?: string;
    displayName?: string;
    avatarUrl?: string;
  } | null>(null);

  const currentUserParticipant = conversation.participants?.find(
    (p) => p.userId === user?.id
  );
  const isAdminOrOwner =
    currentUserParticipant?.role === 'ADMIN' ||
    currentUserParticipant?.role === 'OWNER';

  const otherParticipant = conversation.participants?.find(
    (p) => p.userId !== user?.id
  );

  const handleDirectMessage = async (targetUserId: string, targetName?: string) => {
    try {
      const conv = await conversationsApi.createPrivateConversation(targetUserId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      onClose();
      toast.success(`Chat started with ${targetName || 'user'}`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start chat';
      toast.error(msg);
    }
  };

  const handleRename = async () => {
    if (!newTitle.trim()) return;
    setIsLoading(true);
    try {
      await apiClient(`/api/v1/groups/${conversation.id}/rename`, {
        method: 'PATCH',
        body: JSON.stringify({ title: newTitle.trim() }),
      });
      setIsEditingTitle(false);
      await fetchConversations();
      toast.success('Group renamed successfully');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to rename group';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMemberInput.trim()) return;
    setIsLoading(true);
    try {
      let targetUserId = newMemberInput.trim();
      if (!targetUserId.match(/^[0-9a-fA-F]{24}$/)) {
        const searchRes = await searchApi.searchUsers(newMemberInput.trim(), 0, 1);
        if (searchRes.content.length > 0) {
          targetUserId = searchRes.content[0].userId;
        }
      }

      await apiClient(`/api/v1/groups/${conversation.id}/members`, {
        method: 'POST',
        body: JSON.stringify({ userId: targetUserId }),
      });
      setNewMemberInput('');
      await fetchConversations();
      toast.success('Member added to group');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to add member';
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemoveMember = async (userId: string) => {
    if (!confirm('Are you sure you want to remove this member?')) return;
    try {
      await apiClient(`/api/v1/groups/${conversation.id}/members/${userId}`, {
        method: 'DELETE',
      });
      await fetchConversations();
      toast.success('Member removed');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to remove member';
      toast.error(msg);
    }
  };

  const handlePromoteAdmin = async (userId: string) => {
    try {
      await apiClient(`/api/v1/groups/${conversation.id}/admins/${userId}`, {
        method: 'POST',
      });
      await fetchConversations();
      toast.success('Member promoted to admin');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to promote member';
      toast.error(msg);
    }
  };

  const handleDemoteAdmin = async (userId: string) => {
    try {
      await apiClient(`/api/v1/groups/${conversation.id}/admins/${userId}`, {
        method: 'DELETE',
      });
      await fetchConversations();
      toast.success('Admin demoted to member');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to demote admin';
      toast.error(msg);
    }
  };

  const handleLeaveGroup = async () => {
    if (!confirm('Are you sure you want to leave this group?')) return;
    try {
      await apiClient(`/api/v1/groups/${conversation.id}/leave`, {
        method: 'POST',
      });
      await fetchConversations();
      setActiveConversationId(null);
      toast.success('Left group');
      onClose();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to leave group';
      toast.error(msg);
    }
  };

  return (
    <aside
      className="w-full md:w-80 lg:w-96 h-full flex flex-col bg-slate-950 border-l border-slate-800/80 z-20 select-none overflow-hidden"
      aria-label="Conversation Details Inspector"
    >
      {/* Header */}
      <div className="h-16 px-4 flex items-center justify-between border-b border-slate-800/80 bg-slate-900/80">
        <div className="flex items-center gap-2">
          <Info className="w-4 h-4 text-indigo-400" />
          <h3 className="text-sm font-bold text-white tracking-tight">Details</h3>
        </div>
        <button
          onClick={onClose}
          className="p-1.5 text-slate-400 hover:text-white rounded-xl hover:bg-slate-800 transition-colors"
          aria-label="Close inspector"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      <div className="flex-1 overflow-y-auto p-4 space-y-6">
        {/* Profile / Group Header Card */}
        <div className="text-center p-6 bg-slate-900/60 rounded-3xl border border-slate-800 space-y-3">
          <div
            className={cn(
              'mx-auto inline-block',
              conversation.type === 'DIRECT' && otherParticipant && 'cursor-pointer hover:opacity-90'
            )}
            onClick={() => conversation.type === 'DIRECT' && otherParticipant && setSelectedParticipantForProfile(otherParticipant)}
            title={conversation.type === 'DIRECT' ? 'View profile' : undefined}
          >
            <Avatar
              name={
                conversation.type === 'DIRECT'
                  ? otherParticipant?.displayName || otherParticipant?.username || 'User'
                  : conversation.title || 'Group'
              }
              src={otherParticipant?.avatarUrl}
              size="xl"
            />
          </div>

          <div>
            {conversation.type === 'GROUP' && isEditingTitle ? (
              <div className="flex items-center gap-1.5 justify-center mt-2">
                <Input
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="Group name"
                  className="text-xs h-8"
                />
                <Button size="sm" onClick={handleRename} isLoading={isLoading}>
                  <Check className="w-3.5 h-3.5" />
                </Button>
              </div>
            ) : (
              <div className="flex items-center justify-center gap-2">
                <h4
                  className={cn(
                    'text-base font-bold text-white tracking-tight',
                    conversation.type === 'DIRECT' && otherParticipant && 'cursor-pointer hover:text-indigo-300 transition-colors'
                  )}
                  onClick={() => conversation.type === 'DIRECT' && otherParticipant && setSelectedParticipantForProfile(otherParticipant)}
                >
                  {conversation.type === 'DIRECT'
                    ? otherParticipant?.displayName || otherParticipant?.username
                    : conversation.title}
                </h4>
                {conversation.type === 'GROUP' && isAdminOrOwner && (
                  <button
                    onClick={() => setIsEditingTitle(true)}
                    className="p-1 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800"
                    aria-label="Edit title"
                  >
                    <Edit2 className="w-3.5 h-3.5" />
                  </button>
                )}
              </div>
            )}

            <p className="text-xs text-slate-400 mt-0.5">
              {conversation.type === 'GROUP'
                ? `${conversation.participants?.length || 0} participants`
                : `@${otherParticipant?.username || 'user'}`}
            </p>
          </div>

          <div className="flex items-center justify-center gap-2 pt-2 border-t border-slate-800/80 text-[11px] text-slate-400">
            <Calendar className="w-3.5 h-3.5 text-slate-500" />
            <span>Created {formatConversationTime(conversation.createdAt)}</span>
          </div>

          {conversation.type === 'DIRECT' && otherParticipant && (
            <Button
              size="sm"
              variant="outline"
              onClick={() => setSelectedParticipantForProfile(otherParticipant)}
              className="w-full mt-2 rounded-xl gap-1.5 text-xs"
            >
              <UserIcon className="w-3.5 h-3.5" />
              <span>View Profile</span>
            </Button>
          )}
        </div>

        {/* Group Participants Section */}
        {conversation.type === 'GROUP' && (
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-slate-400 uppercase tracking-wider">
              <span>Participants ({conversation.participants?.length || 0})</span>
              {isAdminOrOwner && (
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => setIsAddMemberOpen(true)}
                  className="h-7 px-2 rounded-lg text-xs text-indigo-400 hover:text-indigo-300 gap-1 hover:bg-indigo-600/10"
                  title="Add new member"
                >
                  <UserPlus className="w-3.5 h-3.5" />
                  <span>Add People</span>
                </Button>
              )}
            </div>

            {/* Participants list */}
            <div className="space-y-1.5 max-h-72 overflow-y-auto pr-1">
              {conversation.participants?.map((p) => {
                const isSelf = p.userId === user?.id;
                return (
                  <div
                    key={p.userId}
                    className="flex items-center justify-between p-2.5 rounded-2xl bg-slate-900/60 border border-slate-800/80"
                  >
                    <div
                      className="flex items-center gap-2.5 min-w-0 cursor-pointer flex-1 mr-2"
                      onClick={() => setSelectedParticipantForProfile(p)}
                      title="View user profile"
                    >
                      <Avatar
                        name={p.displayName || p.username || `User ${p.userId.slice(-4)}`}
                        src={p.avatarUrl}
                        size="sm"
                      />
                      <div className="min-w-0 flex-1">
                        <div className="text-xs font-semibold text-white hover:text-indigo-300 truncate flex items-center gap-1 transition-colors">
                          <span>{p.displayName || (p.username ? `@${p.username}` : `User ${p.userId.slice(-4)}`)}</span>
                          {isSelf && <span className="text-[10px] text-slate-400 font-normal">(You)</span>}
                        </div>
                        <div className="text-[10px] text-slate-400 truncate flex items-center gap-1.5">
                          {p.username && <span className="font-medium text-slate-300">@{p.username}</span>}
                          {p.username && <span>•</span>}
                          <span>{p.role}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1.5">
                      {!isSelf && (
                        <button
                          onClick={() => handleDirectMessage(p.userId, p.displayName || p.username)}
                          className="p-1 rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-slate-800 transition-colors"
                          title="Direct Message"
                          aria-label="Direct message"
                        >
                          <MessageSquare className="w-3.5 h-3.5" />
                        </button>
                      )}

                      <Badge
                        variant={
                          p.role === 'OWNER'
                            ? 'primary'
                            : p.role === 'ADMIN'
                            ? 'warning'
                            : 'secondary'
                        }
                        className="text-[10px]"
                      >
                        {p.role}
                      </Badge>

                      {isAdminOrOwner && !isSelf && p.role !== 'OWNER' && (
                        <div className="flex items-center gap-1">
                          {p.role === 'ADMIN' ? (
                            <button
                              onClick={() => handleDemoteAdmin(p.userId)}
                              className="p-1 rounded-lg text-amber-400 hover:bg-slate-800"
                              title="Demote to Member"
                              aria-label="Demote to Member"
                            >
                              <Shield className="w-3.5 h-3.5" />
                            </button>
                          ) : (
                            <button
                              onClick={() => handlePromoteAdmin(p.userId)}
                              className="p-1 rounded-lg text-slate-400 hover:text-amber-400 hover:bg-slate-800"
                              title="Promote to Admin"
                              aria-label="Promote to Admin"
                            >
                              <Shield className="w-3.5 h-3.5" />
                            </button>
                          )}
                          <button
                            onClick={() => handleRemoveMember(p.userId)}
                            className="p-1 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800"
                            title="Remove from group"
                            aria-label="Remove member"
                          >
                            <UserMinus className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Leave Group Action */}
            <div className="pt-4 border-t border-slate-800/80">
              <Button
                variant="danger"
                size="sm"
                onClick={handleLeaveGroup}
                className="w-full rounded-2xl"
              >
                <LogOut className="w-4 h-4 mr-1.5" />
                <span>Leave Group</span>
              </Button>
            </div>
          </div>
        )}
      </div>

      {/* User Profile Modal */}
      <UserProfileModal
        userId={selectedParticipantForProfile?.userId || null}
        isOpen={!!selectedParticipantForProfile}
        onClose={() => setSelectedParticipantForProfile(null)}
        fallbackUsername={selectedParticipantForProfile?.username}
        fallbackDisplayName={selectedParticipantForProfile?.displayName}
        fallbackAvatarUrl={selectedParticipantForProfile?.avatarUrl}
      />

      {/* Add Member Modal for Group Owners/Admins */}
      {conversation.type === 'GROUP' && (
        <AddMemberModal
          isOpen={isAddMemberOpen}
          onClose={() => setIsAddMemberOpen(false)}
          conversation={conversation}
        />
      )}
    </aside>
  );
};

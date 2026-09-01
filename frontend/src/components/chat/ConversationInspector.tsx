import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { Conversation, User } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { apiClient } from '@/api/client';
import { searchApi } from '@/api';
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

  const currentUserParticipant = conversation.participants?.find(
    (p) => p.userId === user?.id
  );
  const isAdminOrOwner =
    currentUserParticipant?.role === 'ADMIN' ||
    currentUserParticipant?.role === 'OWNER';

  const otherParticipant = conversation.participants?.find(
    (p) => p.userId !== user?.id
  );

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
          <Avatar
            name={
              conversation.type === 'DIRECT'
                ? otherParticipant?.displayName || otherParticipant?.username || 'User'
                : conversation.title || 'Group'
            }
            src={otherParticipant?.avatarUrl}
            size="xl"
            className="mx-auto"
          />

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
                <h4 className="text-base font-bold text-white tracking-tight">
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
        </div>

        {/* Group Participants Section */}
        {conversation.type === 'GROUP' && (
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-slate-400 uppercase tracking-wider">
              <span>Participants ({conversation.participants?.length || 0})</span>
            </div>

            {/* Add Member Form for Admins */}
            {isAdminOrOwner && (
              <form onSubmit={handleAddMember} className="flex items-center gap-2">
                <Input
                  value={newMemberInput}
                  onChange={(e) => setNewMemberInput(e.target.value)}
                  placeholder="Username or User ID..."
                  className="bg-slate-900 text-xs"
                />
                <Button type="submit" size="sm" isLoading={isLoading} className="rounded-xl">
                  <UserPlus className="w-3.5 h-3.5" />
                </Button>
              </form>
            )}

            {/* Participants list */}
            <div className="space-y-1.5 max-h-72 overflow-y-auto pr-1">
              {conversation.participants?.map((p) => {
                const isSelf = p.userId === user?.id;
                return (
                  <div
                    key={p.userId}
                    className="flex items-center justify-between p-2.5 rounded-2xl bg-slate-900/60 border border-slate-800/80"
                  >
                    <div className="flex items-center gap-2.5 min-w-0">
                      <Avatar
                        name={p.displayName || p.username || `User ${p.userId.slice(-4)}`}
                        src={p.avatarUrl}
                        size="sm"
                      />
                      <div className="min-w-0">
                        <div className="text-xs font-semibold text-white truncate flex items-center gap-1">
                          <span>{p.displayName || p.username || `User ${p.userId.slice(-4)}`}</span>
                          {isSelf && <span className="text-[10px] text-slate-400 font-normal">(You)</span>}
                        </div>
                        <div className="text-[10px] text-slate-400 truncate">
                          {p.role}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-1.5">
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
    </aside>
  );
};

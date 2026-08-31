import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { Conversation, User } from '@/types';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { apiClient } from '@/api/client';
import { conversationsApi, searchApi } from '@/api';
import { UserPlus, Shield, UserMinus, LogOut, Edit2, Check } from 'lucide-react';

interface GroupSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  conversation: Conversation;
}

export const GroupSettingsModal: React.FC<GroupSettingsModalProps> = ({
  isOpen,
  onClose,
  conversation,
}) => {
  const { user } = useAuthStore();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [newTitle, setNewTitle] = useState(conversation.title || '');
  const [newMemberInput, setNewMemberInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentUserParticipant = conversation.participants.find((p) => p.userId === user?.id);
  const isAdminOrOwner =
    currentUserParticipant?.role === 'ADMIN' || currentUserParticipant?.role === 'OWNER';

  const handleRename = async () => {
    if (!newTitle.trim()) return;
    setIsLoading(true);
    setError(null);
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
      setError(msg);
      toast.error(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newMemberInput.trim()) return;
    setIsLoading(true);
    setError(null);
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
      setError(msg);
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
      setError(msg);
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
      setError(msg);
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
      setError(msg);
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
      setError(msg);
      toast.error(msg);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Group Settings"
      description="Manage group information and participants"
      maxWidth="lg"
    >
      <div className="space-y-6">
        {error && (
          <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-400">
            {error}
          </div>
        )}

        {/* Group Name Section */}
        <div className="p-4 bg-slate-950/60 rounded-xl border border-slate-800 space-y-2">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Group Name</div>
          {isEditingTitle ? (
            <div className="flex items-center gap-2">
              <Input
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                placeholder="New group name"
                className="bg-slate-900"
              />
              <Button size="sm" onClick={handleRename} isLoading={isLoading}>
                <Check className="w-4 h-4" />
              </Button>
            </div>
          ) : (
            <div className="flex items-center justify-between">
              <span className="text-base font-semibold text-white">{conversation.title}</span>
              {isAdminOrOwner && (
                <button
                  onClick={() => setIsEditingTitle(true)}
                  className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800"
                >
                  <Edit2 className="w-4 h-4" />
                </button>
              )}
            </div>
          )}
        </div>

        {/* Add Member Form (Admin only) */}
        {isAdminOrOwner && (
          <form onSubmit={handleAddMember} className="space-y-2">
            <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Add Member</div>
            <div className="flex items-center gap-2">
              <Input
                value={newMemberInput}
                onChange={(e) => setNewMemberInput(e.target.value)}
                placeholder="Enter username or User ID..."
                className="bg-slate-950"
              />
              <Button type="submit" size="sm" isLoading={isLoading}>
                <UserPlus className="w-4 h-4" />
              </Button>
            </div>
          </form>
        )}

        {/* Member List */}
        <div className="space-y-2">
          <div className="text-xs font-semibold text-slate-400 uppercase tracking-wider">
            Participants ({conversation.participants.length})
          </div>
          <div className="max-h-60 overflow-y-auto space-y-1.5 pr-1">
            {conversation.participants.map((p) => {
              const isCurrentUser = p.userId === user?.id;
              return (
                <div
                  key={p.userId}
                  className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/60 border border-slate-800/80"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <Avatar name={`User ${p.userId.slice(-4)}`} size="sm" />
                    <div className="min-w-0">
                      <div className="text-sm font-medium text-white flex items-center gap-1.5 truncate">
                        <span>User {p.userId.slice(-6)}</span>
                        {isCurrentUser && <span className="text-xs text-slate-400">(You)</span>}
                      </div>
                      <div className="text-[10px] text-slate-400">ID: {p.userId}</div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Badge
                      variant={
                        p.role === 'OWNER'
                          ? 'primary'
                          : p.role === 'ADMIN'
                          ? 'warning'
                          : 'secondary'
                      }
                    >
                      {p.role}
                    </Badge>

                    {isAdminOrOwner && !isCurrentUser && p.role !== 'OWNER' && (
                      <div className="flex items-center gap-1">
                        {p.role === 'ADMIN' ? (
                          <button
                            onClick={() => handleDemoteAdmin(p.userId)}
                            className="p-1 rounded-lg text-amber-400 hover:bg-slate-800"
                            title="Demote to Member"
                          >
                            <Shield className="w-3.5 h-3.5" />
                          </button>
                        ) : (
                          <button
                            onClick={() => handlePromoteAdmin(p.userId)}
                            className="p-1 rounded-lg text-slate-400 hover:text-amber-400 hover:bg-slate-800"
                            title="Promote to Admin"
                          >
                            <Shield className="w-3.5 h-3.5" />
                          </button>
                        )}
                        <button
                          onClick={() => handleRemoveMember(p.userId)}
                          className="p-1 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-slate-800"
                          title="Remove from group"
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
        </div>

        {/* Leave Group Action */}
        <div className="pt-4 border-t border-slate-800 flex justify-end">
          <Button variant="danger" size="sm" onClick={handleLeaveGroup}>
            <LogOut className="w-4 h-4 mr-1.5" />
            Leave Group
          </Button>
        </div>
      </div>
    </Modal>
  );
};

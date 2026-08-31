import React, { useState } from 'react';
import toast from 'react-hot-toast';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Avatar } from '@/components/ui/Avatar';
import { useChatStore } from '@/stores/chatStore';
import { conversationsApi, searchApi } from '@/api';
import { apiClient } from '@/api/client';
import { Search, UserPlus, Users, MessageSquare, Loader2 } from 'lucide-react';
import { SearchUser } from '@/types';

interface NewChatModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const NewChatModal: React.FC<NewChatModalProps> = ({ isOpen, onClose }) => {
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [tab, setTab] = useState<'DIRECT' | 'GROUP'>('DIRECT');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchUser[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  // Group creation state
  const [groupTitle, setGroupTitle] = useState('');
  const [groupDescription, setGroupDescription] = useState('');
  const [selectedUserIds, setSelectedUserIds] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setIsSearching(true);
    setError(null);
    try {
      const res = await searchApi.searchUsers(searchQuery.trim(), 0, 10);
      setSearchResults(res.content || []);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Search failed';
      setError(msg);
      toast.error(msg);
    } finally {
      setIsSearching(false);
    }
  };

  const handleStartDirectChat = async (userId: string) => {
    setIsSubmitting(true);
    setError(null);
    try {
      const conv = await conversationsApi.createPrivateConversation(userId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      toast.success('Conversation started!');
      onClose();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start conversation';
      setError(msg);
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!groupTitle.trim()) {
      setError('Group name is required');
      toast.error('Please enter a group name');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {
      const res = await apiClient<{ id?: string; groupId?: string }>('/api/v1/groups', {
        method: 'POST',
        body: JSON.stringify({
          title: groupTitle.trim(),
          description: groupDescription.trim(),
          initialMemberIds: selectedUserIds,
        }),
      });

      await fetchConversations();
      const groupId = res.id || res.groupId;
      if (groupId) {
        setActiveConversationId(groupId);
      }
      toast.success('Group created successfully!');
      onClose();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create group';
      setError(msg);
      toast.error(msg);
    } finally {
      setIsSubmitting(false);
    }
  };

  const toggleSelectUser = (userId: string) => {
    setSelectedUserIds((prev) =>
      prev.includes(userId) ? prev.filter((id) => id !== userId) : [...prev, userId]
    );
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="New Conversation"
      description="Start a direct message or create a group chat"
      maxWidth="md"
    >
      <div className="space-y-4">
        {/* Tabs */}
        <div className="flex bg-slate-950 p-1 rounded-xl border border-slate-800">
          <button
            onClick={() => {
              setTab('DIRECT');
              setError(null);
            }}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all flex items-center justify-center gap-1.5 ${
              tab === 'DIRECT'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <MessageSquare className="w-3.5 h-3.5" />
            Direct Message
          </button>
          <button
            onClick={() => {
              setTab('GROUP');
              setError(null);
            }}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg transition-all flex items-center justify-center gap-1.5 ${
              tab === 'GROUP'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Users className="w-3.5 h-3.5" />
            New Group
          </button>
        </div>

        {error && (
          <div className="p-3 bg-rose-500/10 border border-rose-500/30 rounded-xl text-xs text-rose-400">
            {error}
          </div>
        )}

        {tab === 'DIRECT' ? (
          <div className="space-y-4">
            {/* User Search Form */}
            <form onSubmit={handleSearch} className="flex gap-2">
              <Input
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search users by username or email..."
                leftIcon={<Search className="w-4 h-4 text-slate-500" />}
                className="bg-slate-950"
              />
              <Button type="submit" size="sm" isLoading={isSearching}>
                Search
              </Button>
            </form>

            {/* Results List */}
            <div className="max-h-64 overflow-y-auto space-y-1.5">
              {searchResults.length > 0 ? (
                searchResults.map((u) => (
                  <div
                    key={u.userId}
                    className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <Avatar name={u.displayName || u.username} src={u.avatarUrl} size="sm" />
                      <div>
                        <div className="text-sm font-semibold text-white">
                          {u.displayName || u.username}
                        </div>
                        <div className="text-xs text-slate-400">@{u.username}</div>
                      </div>
                    </div>
                    <Button
                      size="sm"
                      onClick={() => handleStartDirectChat(u.userId)}
                      isLoading={isSubmitting}
                    >
                      Chat
                    </Button>
                  </div>
                ))
              ) : searchQuery && !isSearching ? (
                <div className="text-center py-6 text-xs text-slate-500">
                  No users found matching "{searchQuery}"
                </div>
              ) : (
                <div className="text-center py-6 text-xs text-slate-500">
                  Search for a user to start messaging
                </div>
              )}
            </div>
          </div>
        ) : (
          /* Group Creation Form */
          <form onSubmit={handleCreateGroup} className="space-y-4">
            <Input
              label="Group Name"
              value={groupTitle}
              onChange={(e) => setGroupTitle(e.target.value)}
              placeholder="e.g. Project Alpha, Family & Friends"
              required
              className="bg-slate-950"
            />
            <Input
              label="Description (Optional)"
              value={groupDescription}
              onChange={(e) => setGroupDescription(e.target.value)}
              placeholder="Brief summary of what this group is for..."
              className="bg-slate-950"
            />

            <div className="pt-2">
              <Button
                type="submit"
                className="w-full"
                isLoading={isSubmitting}
                disabled={!groupTitle.trim()}
              >
                <Users className="w-4 h-4 mr-2" />
                Create Group
              </Button>
            </div>
          </form>
        )}
      </div>
    </Modal>
  );
};

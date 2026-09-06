import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { Modal } from '@/components/ui/Modal';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { Avatar } from '@/components/ui/Avatar';
import { Badge } from '@/components/ui/Badge';
import { useChatStore } from '@/stores/chatStore';
import { groupsApi, searchApi } from '@/api';
import { SearchUser, Conversation } from '@/types';
import { Search, UserPlus, Check, Loader2, Users } from 'lucide-react';

interface AddMemberModalProps {
  isOpen: boolean;
  onClose: () => void;
  conversation: Conversation;
}

export const AddMemberModal: React.FC<AddMemberModalProps> = ({
  isOpen,
  onClose,
  conversation,
}) => {
  const { fetchConversations, fetchMessages } = useChatStore();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchUser[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [addingUserId, setAddingUserId] = useState<string | null>(null);

  // Set of existing member IDs for fast lookup
  const existingMemberIds = new Set(conversation.participants?.map((p) => p.userId) || []);

  useEffect(() => {
    if (!isOpen) {
      setSearchQuery('');
      setSearchResults([]);
      return;
    }

    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const res = await searchApi.searchUsers(searchQuery.trim(), 0, 15);
        setSearchResults(res.content || []);
      } catch (err: unknown) {
        console.error('Failed to search users:', err);
      } finally {
        setIsSearching(false);
      }
    }, 250);

    return () => clearTimeout(timer);
  }, [searchQuery, isOpen]);

  const handleAddMember = async (targetUser: SearchUser) => {
    setAddingUserId(targetUser.userId);
    try {
      await groupsApi.addMember(conversation.id, targetUser.userId);
      toast.success(`Added @${targetUser.username} to ${conversation.title}`);
      await fetchConversations();
      await fetchMessages(conversation.id);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to add member to group';
      toast.error(msg);
    } finally {
      setAddingUserId(null);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Add People to Group"
      description={`Add new participants to "${conversation.title}"`}
      maxWidth="md"
    >
      <div className="space-y-4 pt-2">
        <div className="relative">
          <Input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by username or display name..."
            leftIcon={<Search className="w-4 h-4 text-slate-400" />}
            className="bg-slate-950/60"
            autoFocus
          />
        </div>

        {/* Results List */}
        <div className="min-h-[200px] max-h-72 overflow-y-auto space-y-1.5 pr-1">
          {isSearching ? (
            <div className="flex flex-col items-center justify-center py-10 text-slate-400 space-y-2">
              <Loader2 className="w-6 h-6 animate-spin text-indigo-400" />
              <span className="text-xs">Searching users...</span>
            </div>
          ) : searchResults.length > 0 ? (
            searchResults.map((user) => {
              const isAlreadyMember = existingMemberIds.has(user.userId);
              const isAdding = addingUserId === user.userId;

              return (
                <div
                  key={user.userId}
                  className="flex items-center justify-between p-2.5 rounded-2xl bg-slate-950/40 border border-slate-800/80 hover:border-slate-700/80 transition-all"
                >
                  <div className="flex items-center gap-3 min-w-0 flex-1 mr-2">
                    <Avatar
                      name={user.displayName || user.username}
                      src={user.avatarUrl}
                      size="md"
                    />
                    <div className="min-w-0 flex-1">
                      <div className="text-xs font-semibold text-white truncate flex items-center gap-1.5">
                        <span>{user.displayName || user.username}</span>
                        {user.displayName && (
                          <span className="text-[11px] text-slate-400 font-normal">
                            @{user.username}
                          </span>
                        )}
                      </div>
                      {!user.displayName && (
                        <div className="text-[11px] text-slate-400 truncate">
                          @{user.username}
                        </div>
                      )}
                    </div>
                  </div>

                  {isAlreadyMember ? (
                    <Badge variant="secondary" className="text-[10px] gap-1 px-2.5 py-1">
                      <Check className="w-3 h-3 text-emerald-400" />
                      <span>Member</span>
                    </Badge>
                  ) : (
                    <Button
                      size="sm"
                      onClick={() => handleAddMember(user)}
                      isLoading={isAdding}
                      className="rounded-xl h-8 px-3 text-xs gap-1.5"
                    >
                      <UserPlus className="w-3.5 h-3.5" />
                      <span>Add</span>
                    </Button>
                  )}
                </div>
              );
            })
          ) : searchQuery.trim() ? (
            <div className="text-center py-10 text-xs text-slate-400 select-none">
              No users found matching "{searchQuery}".
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-10 text-slate-500 space-y-2 select-none">
              <Users className="w-8 h-8 text-slate-600 stroke-[1.5]" />
              <span className="text-xs">Type a username to find people</span>
            </div>
          )}
        </div>

        <div className="flex justify-end pt-2 border-t border-slate-800/80">
          <Button variant="secondary" size="sm" onClick={onClose} className="rounded-xl">
            Done
          </Button>
        </div>
      </div>
    </Modal>
  );
};

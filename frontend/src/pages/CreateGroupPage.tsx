import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChatStore } from '@/stores/chatStore';
import { useAuthStore } from '@/stores/authStore';
import { searchApi, groupsApi } from '@/api';
import { SearchUser } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { MobileHeader } from '@/components/navigation/MobileHeader';
import { Users, Search, UserPlus, X, Check } from 'lucide-react';
import toast from 'react-hot-toast';
import { cn } from '@/utils/cn';

export const CreateGroupPage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [groupTitle, setGroupTitle] = useState('');
  const [groupDescription, setGroupDescription] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchUser[]>([]);
  const [selectedMembers, setSelectedMembers] = useState<SearchUser[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isCreating, setIsCreating] = useState(false);

  // Search users with debounce
  useEffect(() => {
    if (!searchQuery.trim()) {
      setSearchResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const res = await searchApi.searchUsers(searchQuery.trim(), 0, 15);
        const filtered = (res.content || []).filter((u) => u.userId !== user?.id);
        setSearchResults(filtered);
      } catch (err) {
        console.error('Failed to search users:', err);
      } finally {
        setIsSearching(false);
      }
    }, 250);

    return () => clearTimeout(timer);
  }, [searchQuery, user?.id]);

  const handleToggleMember = (targetUser: SearchUser) => {
    if (selectedMembers.some((m) => m.userId === targetUser.userId)) {
      setSelectedMembers(selectedMembers.filter((m) => m.userId !== targetUser.userId));
    } else {
      setSelectedMembers([...selectedMembers, targetUser]);
    }
  };

  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!groupTitle.trim()) {
      toast.error('Please enter a group title.');
      return;
    }

    setIsCreating(true);
    try {
      const memberIds = selectedMembers.map((m) => m.userId);
      const conv = await groupsApi.createGroup(
        groupTitle.trim(),
        groupDescription.trim() || undefined,
        memberIds
      );

      await fetchConversations();
      setActiveConversationId(conv.id);
      navigate(`/inbox/${conv.id}`);
      toast.success('Group created successfully!');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create group';
      toast.error(msg);
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <div className="h-full w-full flex flex-col bg-slate-950 text-slate-100 overflow-hidden pb-16 md:pb-0">
      <MobileHeader title="Create Group" showBack={true} backTo="/inbox" />

      <div className="flex-1 overflow-y-auto p-4 sm:p-8 max-w-3xl mx-auto w-full space-y-6">
        {/* Header */}
        <div className="hidden md:block">
          <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <span>Create Group</span>
            <UserPlus className="w-6 h-6 text-indigo-400" />
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Create a group channel and add people from your network to start chatting together.
          </p>
        </div>

        <form onSubmit={handleCreateGroup} className="space-y-6">
          {/* Group Details Card */}
          <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-4">
            <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
              <Users className="w-4 h-4 text-indigo-400" />
              <span>Group Details</span>
            </h2>

            <Input
              label="Group Name"
              value={groupTitle}
              onChange={(e) => setGroupTitle(e.target.value)}
              placeholder="e.g. Project Team, Family & Friends..."
              required
              className="bg-slate-950"
            />

            <Input
              label="Description (Optional)"
              value={groupDescription}
              onChange={(e) => setGroupDescription(e.target.value)}
              placeholder="What is this group about?"
              className="bg-slate-950"
            />
          </div>

          {/* Selected Members Chips */}
          {selectedMembers.length > 0 && (
            <div className="p-5 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-300 uppercase tracking-wider">
                  Added Members ({selectedMembers.length})
                </span>
                <button
                  type="button"
                  onClick={() => setSelectedMembers([])}
                  className="text-[11px] text-slate-400 hover:text-rose-400 transition-colors cursor-pointer"
                >
                  Clear all
                </button>
              </div>

              <div className="flex flex-wrap gap-2">
                {selectedMembers.map((m) => (
                  <span
                    key={m.userId}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-indigo-600/20 border border-indigo-500/40 text-indigo-300 text-xs font-medium shadow-sm"
                  >
                    <Avatar name={m.displayName || m.username} src={m.avatarUrl} size="xs" />
                    <span>{m.displayName || m.username}</span>
                    <button
                      type="button"
                      onClick={() => handleToggleMember(m)}
                      className="hover:text-white ml-0.5"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Add People Section */}
          <div className="p-6 rounded-3xl bg-slate-900/60 border border-slate-800 space-y-4">
            <h2 className="text-sm font-bold text-white uppercase tracking-wider flex items-center gap-2">
              <UserPlus className="w-4 h-4 text-indigo-400" />
              <span>Add People to Group</span>
            </h2>

            <div className="relative">
              <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search people by name or username to add..."
                className="w-full bg-slate-950 text-xs sm:text-sm text-slate-100 placeholder-slate-500 border border-slate-800 focus:border-indigo-500 rounded-2xl pl-10 pr-4 py-3 outline-none transition-all"
              />
            </div>

            <div className="space-y-1.5 max-h-64 overflow-y-auto pr-1">
              {isSearching ? (
                <div className="py-6 text-center text-xs text-slate-500">Searching people...</div>
              ) : searchQuery && searchResults.length === 0 ? (
                <div className="py-6 text-center text-xs text-slate-500">
                  No users found matching "{searchQuery}"
                </div>
              ) : (
                searchResults.map((u) => {
                  const isSelected = selectedMembers.some((m) => m.userId === u.userId);
                  return (
                    <div
                      key={u.userId}
                      onClick={() => handleToggleMember(u)}
                      className={cn(
                        'flex items-center justify-between p-3 rounded-2xl border transition-all cursor-pointer select-none',
                        isSelected
                          ? 'bg-indigo-600/15 border-indigo-500/40'
                          : 'bg-slate-950/60 hover:bg-slate-900 border-slate-800/80'
                      )}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <Avatar name={u.displayName || u.username} src={u.avatarUrl} size="sm" />
                        <div className="min-w-0">
                          <div className="text-xs sm:text-sm font-bold text-white truncate">
                            {u.displayName || u.username}
                          </div>
                          <div className="text-[10px] sm:text-xs text-slate-400 truncate">@{u.username}</div>
                        </div>
                      </div>

                      <div
                        className={cn(
                          'w-6 h-6 rounded-xl flex items-center justify-center border transition-colors',
                          isSelected
                            ? 'bg-indigo-600 border-indigo-500 text-white'
                            : 'border-slate-700 bg-slate-900'
                        )}
                      >
                        {isSelected && <Check className="w-4 h-4" />}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>

          {/* Submit */}
          <Button
            type="submit"
            disabled={!groupTitle.trim() || isCreating}
            isLoading={isCreating}
            className="w-full py-3.5 rounded-2xl text-sm font-bold shadow-lg shadow-indigo-600/25 cursor-pointer"
          >
            Create Group ({selectedMembers.length} {selectedMembers.length === 1 ? 'member' : 'members'})
          </Button>
        </form>
      </div>
    </div>
  );
};

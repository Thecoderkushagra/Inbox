import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useChatStore } from '@/stores/chatStore';
import { useAuthStore } from '@/stores/authStore';
import { searchApi, conversationsApi, groupsApi } from '@/api';
import { SearchUser } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { MobileHeader } from '@/components/navigation/MobileHeader';
import { MessageSquare, Users, Search, Plus, X, ArrowRight, Check } from 'lucide-react';
import toast from 'react-hot-toast';
import { cn } from '@/utils/cn';

type ComposeTab = 'direct' | 'group';

export const ComposePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [activeTab, setActiveTab] = useState<ComposeTab>('direct');
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<SearchUser[]>([]);
  const [isSearching, setIsSearching] = useState(false);

  // Group creation state
  const [groupTitle, setGroupTitle] = useState('');
  const [groupDescription, setGroupDescription] = useState('');
  const [selectedMembers, setSelectedMembers] = useState<SearchUser[]>([]);
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
        // Exclude current user from results
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

  // Start Direct Chat
  const handleStartDirectChat = async (targetUser: SearchUser) => {
    setIsCreating(true);
    try {
      const conv = await conversationsApi.createPrivateConversation(targetUser.userId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      navigate(`/inbox/${conv.id}`);
      toast.success(`Chat started with ${targetUser.displayName || targetUser.username}`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start conversation';
      toast.error(msg);
    } finally {
      setIsCreating(false);
    }
  };

  // Toggle member for group
  const handleToggleMember = (targetUser: SearchUser) => {
    if (selectedMembers.some((m) => m.userId === targetUser.userId)) {
      setSelectedMembers(selectedMembers.filter((m) => m.userId !== targetUser.userId));
    } else {
      setSelectedMembers([...selectedMembers, targetUser]);
    }
  };

  // Create Group
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
      {/* Mobile Top App Bar */}
      <MobileHeader
        title="New Conversation"
        showBack={true}
        backTo="/inbox"
      />

      <div className="flex-1 overflow-y-auto p-4 sm:p-8 max-w-3xl mx-auto w-full space-y-6">
        {/* Desktop Header */}
        <div className="hidden md:block">
          <h1 className="text-2xl font-extrabold text-white tracking-tight">
            Start a Conversation
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Choose whether to start a direct encrypted message or create a group channel.
          </p>
        </div>

        {/* Tab Toggle */}
        <div className="flex rounded-2xl bg-slate-900 p-1 border border-slate-800">
          <button
            onClick={() => setActiveTab('direct')}
            className={cn(
              'flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer',
              activeTab === 'direct'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            )}
          >
            <MessageSquare className="w-4 h-4" />
            <span>Direct Message</span>
          </button>
          <button
            onClick={() => setActiveTab('group')}
            className={cn(
              'flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl text-xs font-bold transition-all cursor-pointer',
              activeTab === 'group'
                ? 'bg-indigo-600 text-white shadow-md'
                : 'text-slate-400 hover:text-white'
            )}
          >
            <Users className="w-4 h-4" />
            <span>New Group Channel</span>
          </button>
        </div>

        {/* DIRECT MESSAGE WORKSPACE */}
        {activeTab === 'direct' && (
          <div className="space-y-4">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                placeholder="Search people by username or name..."
                autoFocus
                className="w-full bg-slate-900 text-sm text-slate-100 placeholder-slate-500 border border-slate-800 focus:border-indigo-500 rounded-2xl pl-10 pr-4 py-3 outline-none transition-all shadow-inner"
              />
            </div>

            {/* Results List */}
            <div className="space-y-2">
              {isSearching ? (
                <div className="p-8 text-center text-xs text-slate-500">Searching people...</div>
              ) : searchQuery && searchResults.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  No users found matching "{searchQuery}"
                </div>
              ) : (
                searchResults.map((u) => (
                  <div
                    key={u.userId}
                    onClick={() => handleStartDirectChat(u)}
                    className="flex items-center justify-between p-3.5 rounded-2xl bg-slate-900/60 hover:bg-slate-900 border border-slate-800/80 hover:border-indigo-500/30 transition-all cursor-pointer group"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <Avatar name={u.displayName || u.username} src={u.avatarUrl} size="md" />
                      <div className="min-w-0">
                        <div className="text-sm font-bold text-white group-hover:text-indigo-300 truncate transition-colors">
                          {u.displayName || u.username}
                        </div>
                        <div className="text-xs text-slate-400 truncate">@{u.username}</div>
                      </div>
                    </div>

                    <Button size="sm" className="rounded-xl flex-shrink-0">
                      <span>Message</span>
                      <ArrowRight className="w-3.5 h-3.5 ml-1" />
                    </Button>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {/* GROUP CHANNEL WORKSPACE */}
        {activeTab === 'group' && (
          <form onSubmit={handleCreateGroup} className="space-y-5">
            <div className="space-y-3 bg-slate-900/60 p-5 rounded-3xl border border-slate-800">
              <Input
                label="Group Channel Name"
                value={groupTitle}
                onChange={(e) => setGroupTitle(e.target.value)}
                placeholder="e.g. Engineering Core, Design Sync..."
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
              <div className="space-y-2">
                <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                  Selected Participants ({selectedMembers.length})
                </label>
                <div className="flex flex-wrap gap-2">
                  {selectedMembers.map((m) => (
                    <span
                      key={m.userId}
                      className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-indigo-600/20 border border-indigo-500/40 text-indigo-300 text-xs font-medium"
                    >
                      <span>{m.displayName || m.username}</span>
                      <button
                        type="button"
                        onClick={() => handleToggleMember(m)}
                        className="hover:text-white"
                      >
                        <X className="w-3 h-3" />
                      </button>
                    </span>
                  ))}
                </div>
              </div>
            )}

            {/* Member Search */}
            <div className="space-y-2">
              <label className="text-xs font-bold text-slate-400 uppercase tracking-wider">
                Add Participants
              </label>
              <div className="relative">
                <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search users to add..."
                  className="w-full bg-slate-900 text-xs text-slate-100 placeholder-slate-500 border border-slate-800 focus:border-indigo-500 rounded-2xl pl-10 pr-4 py-2.5 outline-none transition-all"
                />
              </div>

              <div className="space-y-1.5 max-h-56 overflow-y-auto pr-1">
                {searchResults.map((u) => {
                  const isSelected = selectedMembers.some((m) => m.userId === u.userId);
                  return (
                    <div
                      key={u.userId}
                      onClick={() => handleToggleMember(u)}
                      className={cn(
                        'flex items-center justify-between p-2.5 rounded-2xl border transition-all cursor-pointer select-none',
                        isSelected
                          ? 'bg-indigo-600/15 border-indigo-500/40'
                          : 'bg-slate-900/60 hover:bg-slate-900 border-slate-800/80'
                      )}
                    >
                      <div className="flex items-center gap-2.5 min-w-0">
                        <Avatar name={u.displayName || u.username} src={u.avatarUrl} size="sm" />
                        <div className="min-w-0">
                          <div className="text-xs font-bold text-white truncate">
                            {u.displayName || u.username}
                          </div>
                          <div className="text-[10px] text-slate-400 truncate">@{u.username}</div>
                        </div>
                      </div>

                      <div
                        className={cn(
                          'w-5 h-5 rounded-lg flex items-center justify-center border transition-colors',
                          isSelected
                            ? 'bg-indigo-600 border-indigo-500 text-white'
                            : 'border-slate-700 bg-slate-950'
                        )}
                      >
                        {isSelected && <Check className="w-3.5 h-3.5" />}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <Button
              type="submit"
              disabled={!groupTitle.trim() || isCreating}
              isLoading={isCreating}
              className="w-full py-3 rounded-2xl text-sm font-bold"
            >
              Create Group Channel
            </Button>
          </form>
        )}
      </div>
    </div>
  );
};

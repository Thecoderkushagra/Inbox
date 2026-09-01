import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchApi, conversationsApi } from '@/api';
import { SearchUser } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { MobileHeader } from '@/components/navigation/MobileHeader';
import { EmptyState } from '@/components/ui/EmptyState';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { Users, Search, MessageSquare, Plus, ArrowRight, Mail } from 'lucide-react';
import toast from 'react-hot-toast';

export const PeoplePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [query, setQuery] = useState('');
  const [people, setPeople] = useState<SearchUser[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  const fetchPeople = async (keyword: string) => {
    setIsLoading(true);
    try {
      const res = await searchApi.searchUsers(keyword || 'a', 0, 30);
      const filtered = (res.content || []).filter((u) => u.userId !== user?.id);
      setPeople(filtered);
    } catch (err) {
      console.error('Failed to fetch people:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPeople(query);
  }, [query]);

  const handleStartChat = async (targetUser: SearchUser) => {
    try {
      const conv = await conversationsApi.createPrivateConversation(targetUser.userId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      navigate(`/inbox/${conv.id}`);
      toast.success(`Chat started with ${targetUser.displayName || targetUser.username}`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start chat';
      toast.error(msg);
    }
  };

  return (
    <div className="h-full w-full flex flex-col bg-slate-950 text-slate-100 overflow-hidden pb-16 md:pb-0">
      <MobileHeader title="People Directory" showBack={false} />

      <div className="flex-1 overflow-y-auto p-4 sm:p-8 max-w-5xl mx-auto w-full space-y-6">
        {/* Desktop Header */}
        <div className="hidden md:flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
              <span>People &amp; Contacts</span>
              <Users className="w-6 h-6 text-indigo-400" />
            </h1>
            <p className="text-xs text-slate-400 mt-1">
              Find and connect securely with colleagues, teammates, and contacts.
            </p>
          </div>
        </div>

        {/* Search input */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search directory by name, username..."
            className="w-full bg-slate-900 text-xs sm:text-sm text-slate-100 placeholder-slate-500 border border-slate-800 focus:border-indigo-500 rounded-2xl pl-10 pr-4 py-3 outline-none transition-all shadow-inner"
          />
        </div>

        {/* Directory Grid */}
        {isLoading ? (
          <div className="py-16 text-center text-xs text-slate-500">Loading contacts...</div>
        ) : people.length === 0 ? (
          <EmptyState
            icon={<Users className="w-8 h-8" />}
            title="No contacts found"
            description={
              query
                ? `No users matched "${query}".`
                : 'No users currently found in the directory.'
            }
            className="my-12"
          />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {people.map((p) => (
              <div
                key={p.userId}
                className="p-4 rounded-3xl bg-slate-900/60 hover:bg-slate-900 border border-slate-800/80 hover:border-indigo-500/30 transition-all flex flex-col justify-between space-y-4 group shadow-sm"
              >
                <div className="flex items-start gap-3 min-w-0">
                  <Avatar
                    name={p.displayName || p.username}
                    src={p.avatarUrl}
                    size="lg"
                    status="ONLINE"
                  />
                  <div className="min-w-0 flex-1">
                    <h3 className="text-sm font-bold text-white group-hover:text-indigo-300 truncate transition-colors">
                      {p.displayName || p.username}
                    </h3>
                    <p className="text-xs text-slate-400 truncate">@{p.username}</p>
                    {p.bio && (
                      <p className="text-[11px] text-slate-400 line-clamp-2 mt-1 leading-snug">
                        {p.bio}
                      </p>
                    )}
                  </div>
                </div>

                <div className="pt-2 border-t border-slate-800/80 flex items-center justify-end">
                  <Button
                    size="sm"
                    onClick={() => handleStartChat(p)}
                    className="w-full rounded-xl gap-1.5"
                  >
                    <MessageSquare className="w-3.5 h-3.5" />
                    <span>Send Message</span>
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

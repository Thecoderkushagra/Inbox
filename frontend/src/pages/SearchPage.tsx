import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchApi, conversationsApi } from '@/api';
import { GlobalSearchResponse, SearchUser, SearchConversation, SearchMessage } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { MobileHeader } from '@/components/navigation/MobileHeader';
import { EmptyState } from '@/components/ui/EmptyState';
import { formatMessageTime } from '@/utils/formatDate';
import { useChatStore } from '@/stores/chatStore';
import {
  Search,
  Users,
  MessageSquare,
  Hash,
  ArrowRight,
  X,
  Sparkles,
} from 'lucide-react';
import { cn } from '@/utils/cn';
import toast from 'react-hot-toast';

type SearchFilterTab = 'all' | 'people' | 'groups' | 'messages';

export const SearchPage: React.FC = () => {
  const navigate = useNavigate();
  const { fetchConversations, setActiveConversationId } = useChatStore();

  const [query, setQuery] = useState('');
  const [activeTab, setActiveTab] = useState<SearchFilterTab>('all');
  const [results, setResults] = useState<GlobalSearchResponse | null>(null);
  const [isSearching, setIsSearching] = useState(false);

  useEffect(() => {
    if (!query.trim()) {
      setResults(null);
      return;
    }

    const timer = setTimeout(async () => {
      setIsSearching(true);
      try {
        const res = await searchApi.globalSearch(query.trim());
        setResults(res);
      } catch (err) {
        console.error('Search error:', err);
      } finally {
        setIsSearching(false);
      }
    }, 250);

    return () => clearTimeout(timer);
  }, [query]);

  const handleStartChatWithUser = async (userId: string) => {
    try {
      const conv = await conversationsApi.createPrivateConversation(userId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      navigate(`/inbox/${conv.id}`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to start chat';
      toast.error(msg);
    }
  };

  const usersCount = results?.users?.length || 0;
  const groupsCount = results?.groups?.length || 0;
  const messagesCount = results?.messages?.length || 0;
  const totalResults = usersCount + groupsCount + messagesCount;

  return (
    <div className="h-full w-full flex flex-col bg-slate-950 text-slate-100 overflow-hidden pb-16 md:pb-0">
      <MobileHeader title="Search" showBack={true} backTo="/inbox" />

      <div className="flex-1 overflow-y-auto p-4 sm:p-8 max-w-4xl mx-auto w-full space-y-6">
        {/* Header */}
        <div className="hidden md:block">
          <h1 className="text-2xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <span>Universal Search</span>
            <Sparkles className="w-5 h-5 text-indigo-400" />
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Search seamlessly across people, group channels, and message contents.
          </p>
        </div>

        {/* Big Search Input */}
        <div className="relative">
          <Search className="w-5 h-5 text-slate-500 absolute left-4 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Type anything to search..."
            autoFocus
            className="w-full bg-slate-900 text-sm text-slate-100 placeholder-slate-500 border border-slate-800 focus:border-indigo-500 rounded-3xl pl-12 pr-10 py-3.5 outline-none transition-all shadow-xl"
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="absolute right-3.5 top-1/2 -translate-y-1/2 p-1 text-slate-500 hover:text-slate-200"
              aria-label="Clear search"
            >
              <X className="w-4 h-4" />
            </button>
          )}
        </div>

        {/* Filter Tabs */}
        {query && (
          <div className="flex items-center gap-2 overflow-x-auto pb-1">
            {(
              [
                { id: 'all', label: `All (${totalResults})` },
                { id: 'people', label: `People (${usersCount})` },
                { id: 'groups', label: `Groups (${groupsCount})` },
                { id: 'messages', label: `Messages (${messagesCount})` },
              ] as const
            ).map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={cn(
                  'px-3.5 py-1.5 rounded-full text-xs font-bold transition-all cursor-pointer whitespace-nowrap',
                  activeTab === tab.id
                    ? 'bg-indigo-600 text-white shadow-sm'
                    : 'bg-slate-900 text-slate-400 hover:text-white border border-slate-800'
                )}
              >
                {tab.label}
              </button>
            ))}
          </div>
        )}

        {/* Search Results Display */}
        {isSearching ? (
          <div className="py-16 text-center text-xs text-slate-500">Searching...</div>
        ) : !query ? (
          <EmptyState
            icon={<Search className="w-8 h-8" />}
            title="Start Searching"
            description="Find people by username or name, discover group channels, or search through previous conversations."
            className="my-12"
          />
        ) : totalResults === 0 ? (
          <EmptyState
            icon={<Search className="w-8 h-8" />}
            title="No results found"
            description={`We couldn't find anything matching "${query}". Try searching with different keywords.`}
            className="my-12"
          />
        ) : (
          <div className="space-y-6">
            {/* PEOPLE SECTION */}
            {(activeTab === 'all' || activeTab === 'people') && usersCount > 0 && (
              <section className="space-y-3">
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Users className="w-4 h-4 text-indigo-400" />
                  <span>People ({usersCount})</span>
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  {results?.users.map((u) => (
                    <div
                      key={u.userId}
                      className="p-3 rounded-2xl bg-slate-900/60 border border-slate-800 flex items-center justify-between gap-3 hover:border-indigo-500/30 transition-all"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <Avatar name={u.displayName || u.username} src={u.avatarUrl} size="md" />
                        <div className="min-w-0">
                          <div className="text-sm font-bold text-white truncate">
                            {u.displayName || u.username}
                          </div>
                          <div className="text-xs text-slate-400 truncate">@{u.username}</div>
                        </div>
                      </div>
                      <Button
                        size="sm"
                        onClick={() => handleStartChatWithUser(u.userId)}
                        className="rounded-xl flex-shrink-0"
                      >
                        Chat
                      </Button>
                    </div>
                  ))}
                </div>
              </section>
            )}

            {/* GROUPS SECTION */}
            {(activeTab === 'all' || activeTab === 'groups') && groupsCount > 0 && (
              <section className="space-y-3">
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <Hash className="w-4 h-4 text-indigo-400" />
                  <span>Groups ({groupsCount})</span>
                </h3>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                  {results?.groups.map((g) => (
                    <div
                      key={g.conversationId}
                      onClick={() => {
                        setActiveConversationId(g.conversationId);
                        navigate(`/inbox/${g.conversationId}`);
                      }}
                      className="p-3.5 rounded-2xl bg-slate-900/60 border border-slate-800 flex items-center justify-between gap-3 hover:border-indigo-500/30 transition-all cursor-pointer group"
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div className="w-10 h-10 rounded-full bg-indigo-600/20 text-indigo-400 flex items-center justify-center font-bold">
                          #
                        </div>
                        <div className="min-w-0">
                          <div className="text-sm font-bold text-white group-hover:text-indigo-300 truncate transition-colors">
                            {g.title || 'Group'}
                          </div>
                          <div className="text-xs text-slate-400 truncate">
                            {g.participantCount} members
                          </div>
                        </div>
                      </div>
                      <ArrowRight className="w-4 h-4 text-slate-500 group-hover:text-indigo-400 group-hover:translate-x-0.5 transition-all" />
                    </div>
                  ))}
                </div>
              </section>
            )}

            {/* MESSAGES SECTION */}
            {(activeTab === 'all' || activeTab === 'messages') && messagesCount > 0 && (
              <section className="space-y-3">
                <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider flex items-center gap-1.5">
                  <MessageSquare className="w-4 h-4 text-indigo-400" />
                  <span>Messages ({messagesCount})</span>
                </h3>
                <div className="space-y-2">
                  {results?.messages.map((m) => (
                    <div
                      key={m.messageId}
                      onClick={() => {
                        setActiveConversationId(m.conversationId);
                        navigate(`/inbox/${m.conversationId}`);
                      }}
                      className="p-3.5 rounded-2xl bg-slate-900/60 border border-slate-800 hover:border-indigo-500/30 transition-all cursor-pointer group space-y-1"
                    >
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-bold text-indigo-300">
                          {m.conversationTitle || 'Conversation'}
                        </span>
                        <span className="text-[10px] text-slate-500">
                          {formatMessageTime(m.createdAt)}
                        </span>
                      </div>
                      <p className="text-xs text-slate-300 line-clamp-2 leading-relaxed">
                        {m.content}
                      </p>
                    </div>
                  ))}
                </div>
              </section>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

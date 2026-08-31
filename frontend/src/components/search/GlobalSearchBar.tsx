import React, { useState, useEffect, useRef } from 'react';
import { Search, User as UserIcon, Users, MessageSquare, Loader2, X } from 'lucide-react';
import { searchApi, conversationsApi } from '@/api';
import { GlobalSearchResponse } from '@/types';
import { Avatar } from '@/components/ui/Avatar';
import { useChatStore } from '@/stores/chatStore';

export const GlobalSearchBar: React.FC = () => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<GlobalSearchResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);

  const containerRef = useRef<HTMLDivElement>(null);
  const { fetchConversations, setActiveConversationId } = useChatStore();

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!query.trim() || query.length < 2) {
      setResults(null);
      setIsLoading(false);
      return;
    }

    const timer = setTimeout(async () => {
      setIsLoading(true);
      try {
        const data = await searchApi.globalSearch(query.trim());
        setResults(data);
        setIsOpen(true);
      } catch (err) {
        console.error('Search failed:', err);
      } finally {
        setIsLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  const handleSelectUser = async (userId: string) => {
    try {
      const conv = await conversationsApi.createPrivateConversation(userId);
      await fetchConversations();
      setActiveConversationId(conv.id);
      setIsOpen(false);
      setQuery('');
    } catch (err) {
      console.error('Failed to open chat:', err);
    }
  };

  const handleSelectGroup = (conversationId: string) => {
    setActiveConversationId(conversationId);
    setIsOpen(false);
    setQuery('');
  };

  return (
    <div ref={containerRef} className="relative w-64 md:w-80">
      <div className="relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => {
            if (results) setIsOpen(true);
          }}
          placeholder="Global search..."
          className="w-full bg-slate-900 text-xs text-slate-100 placeholder-slate-500 border border-slate-700/80 rounded-xl pl-9 pr-8 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500 transition-all"
        />
        {isLoading ? (
          <Loader2 className="w-3.5 h-3.5 text-slate-400 animate-spin absolute right-3 top-1/2 -translate-y-1/2" />
        ) : query ? (
          <button
            onClick={() => {
              setQuery('');
              setResults(null);
            }}
            className="p-1 text-slate-400 hover:text-white absolute right-2 top-1/2 -translate-y-1/2"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        ) : null}
      </div>

      {/* Search Results Dropdown */}
      {isOpen && results && (
        <div className="absolute left-0 right-0 mt-2 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden z-50 max-h-96 overflow-y-auto animate-in fade-in zoom-in-95">
          {results.users.length === 0 && results.groups.length === 0 ? (
            <div className="p-4 text-center text-xs text-slate-500">
              No users or groups found for "{query}"
            </div>
          ) : (
            <div className="p-2 space-y-3">
              {/* Users */}
              {results.users.length > 0 && (
                <div>
                  <div className="px-2 py-1 text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                    Users
                  </div>
                  <div className="space-y-0.5">
                    {results.users.map((u) => (
                      <button
                        key={u.userId}
                        onClick={() => handleSelectUser(u.userId)}
                        className="w-full flex items-center gap-2.5 p-2 rounded-xl hover:bg-slate-800 transition-colors text-left"
                      >
                        <Avatar name={u.displayName || u.username} src={u.avatarUrl} size="sm" />
                        <div className="min-w-0">
                          <div className="text-xs font-semibold text-white truncate">
                            {u.displayName || u.username}
                          </div>
                          <div className="text-[10px] text-slate-400">@{u.username}</div>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {/* Groups */}
              {results.groups.length > 0 && (
                <div>
                  <div className="px-2 py-1 text-[10px] font-semibold text-slate-400 uppercase tracking-wider">
                    Groups & Channels
                  </div>
                  <div className="space-y-0.5">
                    {results.groups.map((g) => (
                      <button
                        key={g.conversationId}
                        onClick={() => handleSelectGroup(g.conversationId)}
                        className="w-full flex items-center gap-2.5 p-2 rounded-xl hover:bg-slate-800 transition-colors text-left"
                      >
                        <div className="w-8 h-8 rounded-full bg-indigo-600/20 border border-indigo-500/30 flex items-center justify-center text-indigo-400 flex-shrink-0">
                          <Users className="w-4 h-4" />
                        </div>
                        <div className="min-w-0">
                          <div className="text-xs font-semibold text-white truncate">
                            {g.title}
                          </div>
                          <div className="text-[10px] text-slate-400 truncate">
                            {g.description || 'Group conversation'}
                          </div>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

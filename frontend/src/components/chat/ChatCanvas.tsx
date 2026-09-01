import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { usePresenceStore } from '@/stores/presenceStore';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { MessageBubble } from './MessageBubble';
import { MediaUploader } from './MediaUploader';
import { EmptyState } from '@/components/ui/EmptyState';
import { mediaApi } from '@/api';
import { formatRelativeTime } from '@/utils/formatDate';
import {
  Send,
  Lock,
  FileText,
  X,
  Info,
  ChevronLeft,
  Users,
  Shield,
  MessageSquare,
} from 'lucide-react';
import { cn } from '@/utils/cn';

interface ChatCanvasProps {
  onToggleInspector?: () => void;
  isInspectorOpen?: boolean;
  showMobileBack?: boolean;
}

export const ChatCanvas: React.FC<ChatCanvasProps> = ({
  onToggleInspector,
  isInspectorOpen,
  showMobileBack = false,
}) => {
  const { user } = useAuthStore();
  const {
    conversations,
    messages,
    sendMessage,
    isSendingMessage,
    typingUsers,
    fetchMessages,
  } = useChatStore();
  const { getUserPresence } = usePresenceStore();
  const navigate = useNavigate();
  const { conversationId } = useParams<{ conversationId?: string }>();

  const [inputContent, setInputContent] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const activeConversation = conversations.find((c) => c.id === conversationId);
  const currentMessages = conversationId ? messages[conversationId] || [] : [];

  // Fetch messages on mount or when conversationId changes
  useEffect(() => {
    if (conversationId) {
      fetchMessages(conversationId);
    }
  }, [conversationId, fetchMessages]);

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [currentMessages.length, conversationId]);

  // Determine recipient presence for direct conversations
  const otherParticipant = activeConversation?.participants?.find(
    (p) => p.userId !== user?.id
  );
  const otherPresence = otherParticipant
    ? getUserPresence(otherParticipant.userId)
    : undefined;

  const getDisplayTitle = () => {
    if (!activeConversation) return 'Chat';
    if (activeConversation.type === 'DIRECT') {
      if (otherParticipant?.displayName) return otherParticipant.displayName;
      if (otherParticipant?.username) return otherParticipant.username;
    }
    return activeConversation.title || 'Conversation';
  };

  const displayTitle = getDisplayTitle();

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (
      !conversationId ||
      (!inputContent.trim() && !selectedFile) ||
      isSendingMessage ||
      isUploading
    ) {
      return;
    }

    const content =
      inputContent.trim() ||
      (selectedFile ? `Sent an attachment: ${selectedFile.name}` : '');
    const fileToUpload = selectedFile;

    setInputContent('');
    setSelectedFile(null);

    try {
      const msg = await sendMessage(conversationId, content);

      if (fileToUpload && msg) {
        setIsUploading(true);
        try {
          await mediaApi.uploadAttachment(msg.id, fileToUpload);
          fetchMessages(conversationId);
        } catch (uploadErr) {
          console.error('Failed to upload attachment:', uploadErr);
        } finally {
          setIsUploading(false);
        }
      }
    } catch (err) {
      console.error('Failed to send message:', err);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  if (!activeConversation) {
    return (
      <div className="flex-1 h-full flex flex-col items-center justify-center bg-slate-950/40 p-6 text-center select-none">
        <EmptyState
          icon={<MessageSquare className="w-8 h-8" />}
          title="Select a conversation"
          description="Choose a conversation from the sidebar or start a new chat to begin messaging with end-to-end encryption."
          actionLabel="Start a Chat"
          onAction={() => navigate('/compose')}
        />
      </div>
    );
  }

  return (
    <div className="flex-1 h-full flex flex-col bg-slate-950/40 relative overflow-hidden">
      {/* Top Header */}
      <header className="h-16 px-4 sm:px-6 bg-slate-900/90 backdrop-blur-md border-b border-slate-800/80 flex items-center justify-between z-20 flex-shrink-0">
        <div className="flex items-center gap-3 min-w-0">
          {showMobileBack && (
            <button
              onClick={() => navigate('/inbox')}
              className="md:hidden p-1.5 -ml-2 text-slate-400 hover:text-white rounded-xl active:bg-slate-800 transition-colors"
              aria-label="Back to conversations"
            >
              <ChevronLeft className="w-6 h-6" />
            </button>
          )}

          <Avatar
            name={displayTitle}
            src={otherParticipant?.avatarUrl}
            size="md"
            status={activeConversation.type === 'DIRECT' ? otherPresence?.status : undefined}
          />

          <div className="min-w-0">
            <h2 className="text-sm font-bold text-white tracking-tight truncate flex items-center gap-2">
              <span>{displayTitle}</span>
              {activeConversation.type === 'GROUP' && (
                <span className="text-[10px] px-1.5 py-0.2 rounded bg-indigo-500/20 text-indigo-300 font-semibold">
                  Group
                </span>
              )}
            </h2>
            <p className="text-[11px] text-slate-400 truncate">
              {activeConversation.type === 'DIRECT' ? (
                otherPresence?.status === 'ONLINE' ? (
                  <span className="text-emerald-400 font-medium">Online</span>
                ) : otherPresence?.lastSeen ? (
                  `Last seen ${formatRelativeTime(otherPresence.lastSeen)}`
                ) : (
                  'Offline'
                )
              ) : (
                `${activeConversation.participants?.length || 0} members`
              )}
            </p>
          </div>
        </div>

        {/* Right Header Action */}
        <div className="flex items-center gap-1.5">
          {onToggleInspector && (
            <button
              onClick={onToggleInspector}
              className={cn(
                'p-2 rounded-xl text-xs transition-colors cursor-pointer flex items-center gap-1.5',
                isInspectorOpen
                  ? 'bg-indigo-600/20 text-indigo-300 border border-indigo-500/30'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800'
              )}
              title="Conversation details"
              aria-label="Conversation details"
            >
              <Info className="w-5 h-5" />
            </button>
          )}
        </div>
      </header>

      {/* Message List Scroll Area */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-1">
        {/* Security Banner */}
        <div className="flex justify-center my-2">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-900/80 border border-slate-800 text-[11px] text-slate-400 shadow-sm select-none">
            <Lock className="w-3 h-3 text-indigo-400" />
            <span>Messages are secured and private in this conversation</span>
          </div>
        </div>

        {currentMessages.length === 0 ? (
          <div className="py-12 text-center text-xs text-slate-500">
            No messages yet. Send a message to start the conversation!
          </div>
        ) : (
          currentMessages.map((msg) => (
            <MessageBubble
              key={msg.id}
              message={msg}
              currentUser={user}
              conversationType={activeConversation.type}
            />
          ))
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Typing Indicator */}
      {conversationId &&
        typingUsers[conversationId] &&
        typingUsers[conversationId].size > 0 && (
          <div className="px-6 py-1 text-xs text-indigo-400 animate-pulse flex items-center gap-1.5">
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-400" />
            <span>
              {Array.from(typingUsers[conversationId]).join(', ')} is typing...
            </span>
          </div>
        )}

      {/* Selected File Preview Banner */}
      {selectedFile && (
        <div className="mx-4 mb-2 p-2.5 bg-slate-900 border border-slate-800 rounded-2xl flex items-center justify-between animate-in fade-in">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="p-2 bg-indigo-500/20 text-indigo-400 rounded-xl">
              <FileText className="w-4 h-4" />
            </div>
            <div className="min-w-0">
              <div className="text-xs font-medium text-slate-200 truncate">
                {selectedFile.name}
              </div>
              <div className="text-[10px] text-slate-400">
                {(selectedFile.size / 1024 / 1024).toFixed(2)} MB
              </div>
            </div>
          </div>
          <button
            onClick={() => setSelectedFile(null)}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800"
            aria-label="Remove attachment"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Composer Footer */}
      <footer className="p-3 sm:p-4 bg-slate-900/90 backdrop-blur-md border-t border-slate-800/80">
        <form onSubmit={handleSend} className="flex items-end gap-2 max-w-5xl mx-auto">
          <MediaUploader
            onMediaUploaded={(file) => setSelectedFile(file)}
            disabled={isSendingMessage || isUploading}
          />

          <div className="flex-1 bg-slate-950 border border-slate-800 focus-within:border-indigo-500 rounded-2xl px-4 py-2.5 transition-all">
            <textarea
              ref={textareaRef}
              value={inputContent}
              onChange={(e) => setInputContent(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Type a message... (Enter to send, Shift+Enter for newline)"
              rows={1}
              className="w-full bg-transparent text-xs sm:text-sm text-slate-100 placeholder-slate-500 focus:outline-none resize-none max-h-32 leading-relaxed"
            />
          </div>

          <Button
            type="submit"
            size="icon"
            disabled={
              (!inputContent.trim() && !selectedFile) ||
              isSendingMessage ||
              isUploading
            }
            isLoading={isSendingMessage || isUploading}
            className="rounded-2xl w-11 h-11 flex-shrink-0"
            title="Send message"
            aria-label="Send message"
          >
            <Send className="w-4 h-4" />
          </Button>
        </form>
      </footer>
    </div>
  );
};

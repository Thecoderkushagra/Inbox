import React, { useEffect, useRef, useState } from 'react';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { usePresenceStore } from '@/stores/presenceStore';
import { useWebSocket } from '@/context/WebSocketContext';
import { Avatar } from '@/components/ui/Avatar';
import { Button } from '@/components/ui/Button';
import { MessageBubble } from './MessageBubble';
import { MediaUploader } from './MediaUploader';
import { mediaApi } from '@/api';
import { formatRelativeTime } from '@/utils/formatDate';
import {
  Send,
  MoreVertical,
  Users,
  Shield,
  X,
  FileText,
  Phone,
  Video,
  Lock,
} from 'lucide-react';
import { GroupSettingsModal } from './GroupSettingsModal';

export const ChatWindow: React.FC = () => {
  const { user } = useAuthStore();
  const {
    conversations,
    activeConversationId,
    messages,
    sendMessage,
    isSendingMessage,
    typingUsers,
    fetchMessages,
  } = useChatStore();
  const { getUserPresence } = usePresenceStore();
  const { isConnected } = useWebSocket();

  const [inputContent, setInputContent] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [showGroupModal, setShowGroupModal] = useState(false);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const activeConversation = conversations.find((c) => c.id === activeConversationId);
  const currentMessages = activeConversationId ? messages[activeConversationId] || [] : [];

  // Scroll to bottom on new messages
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [currentMessages.length, activeConversationId]);

  // Determine recipient presence for direct conversations
  const otherParticipant = activeConversation?.participants?.find((p) => p.userId !== user?.id);
  const otherPresence = otherParticipant ? getUserPresence(otherParticipant.userId) : undefined;

  const handleSend = async (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!activeConversationId || (!inputContent.trim() && !selectedFile) || isSendingMessage || isUploading) {
      return;
    }

    const content = inputContent.trim() || (selectedFile ? `Sent an attachment: ${selectedFile.name}` : '');
    const fileToUpload = selectedFile;

    setInputContent('');
    setSelectedFile(null);

    try {
      const msg = await sendMessage(activeConversationId, content);

      if (fileToUpload && msg) {
        setIsUploading(true);
        try {
          await mediaApi.uploadAttachment(msg.id, fileToUpload);
          fetchMessages(activeConversationId);
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
      <div className="flex-1 h-full flex flex-col items-center justify-center bg-slate-950/60 p-6 text-center select-none">
        <div className="w-20 h-20 rounded-3xl bg-indigo-600/10 border border-indigo-500/20 flex items-center justify-center mb-4 shadow-xl">
          <img src="/frontend/src/assets/inbox-logo.png" alt="Inbox" className="w-12 h-12 object-contain" />
        </div>
        <h2 className="text-xl font-bold text-white tracking-tight">Your Messages</h2>
        <p className="text-sm text-slate-400 max-w-sm mt-1">
          Select a conversation from the sidebar or start a new chat to connect securely with end-to-end encryption.
        </p>
        <div className="flex items-center gap-2 mt-4 px-3 py-1.5 rounded-full bg-slate-900 border border-slate-800 text-xs text-slate-400">
          <Lock className="w-3.5 h-3.5 text-indigo-400" />
          <span>Real-time encrypted connection</span>
        </div>
      </div>
    );
  }

  return (
    <div className="flex-1 h-full flex flex-col bg-slate-950/40 relative overflow-hidden">
      {/* Top Conversation Header */}
      <div className="h-16 px-6 bg-slate-900/80 backdrop-blur-md border-b border-slate-800/80 flex items-center justify-between z-10">
        <div className="flex items-center gap-3 min-w-0">
          <Avatar
            name={activeConversation.title || 'Chat'}
            size="md"
            status={activeConversation.type === 'DIRECT' ? otherPresence?.status : undefined}
          />
          <div className="min-w-0">
            <h3 className="text-sm font-semibold text-white truncate flex items-center gap-2">
              {activeConversation.title || 'Chat'}
              {activeConversation.type === 'GROUP' && (
                <span className="text-[10px] px-1.5 py-0.5 rounded bg-indigo-500/20 text-indigo-300 font-normal">
                  Group
                </span>
              )}
            </h3>
            <p className="text-xs text-slate-400 truncate">
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

        {/* Header Action Buttons */}
        <div className="flex items-center gap-1.5 text-slate-400">
          {activeConversation.type === 'GROUP' && (
            <button
              onClick={() => setShowGroupModal(true)}
              className="p-2 rounded-xl hover:bg-slate-800 hover:text-white transition-colors"
              title="Group settings"
            >
              <Users className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>

      {/* Message List Scroll Area */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-1">
        {/* Encryption banner */}
        <div className="flex justify-center my-3">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-900/80 border border-slate-800 text-[11px] text-slate-400 shadow-sm">
            <Lock className="w-3 h-3 text-indigo-400" />
            <span>Messages are secured and private in this conversation</span>
          </div>
        </div>

        {currentMessages.map((msg) => (
          <MessageBubble
            key={msg.id}
            message={msg}
            currentUser={user}
            conversationType={activeConversation.type}
          />
        ))}

        <div ref={messagesEndRef} />
      </div>

      {/* Typing indicator */}
      {activeConversationId &&
        typingUsers[activeConversationId] &&
        typingUsers[activeConversationId].size > 0 && (
          <div className="px-6 py-1 text-xs text-indigo-400 animate-pulse flex items-center gap-1">
            <span className="w-1.5 h-1.5 rounded-full bg-indigo-400"></span>
            <span>
              {Array.from(typingUsers[activeConversationId]).join(', ')} is typing...
            </span>
          </div>
        )}

      {/* Selected File Preview Banner */}
      {selectedFile && (
        <div className="mx-4 mb-2 p-2.5 bg-slate-900 border border-slate-800 rounded-xl flex items-center justify-between animate-in fade-in">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="p-2 bg-indigo-500/20 text-indigo-400 rounded-lg">
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
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Message Composer Footer */}
      <div className="p-4 bg-slate-900/90 backdrop-blur-md border-t border-slate-800/80">
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
              className="w-full bg-transparent text-sm text-slate-100 placeholder-slate-500 focus:outline-none resize-none max-h-32 leading-relaxed"
            />
          </div>

          <Button
            type="submit"
            size="icon"
            disabled={(!inputContent.trim() && !selectedFile) || isSendingMessage || isUploading}
            isLoading={isSendingMessage || isUploading}
            className="rounded-2xl w-11 h-11 flex-shrink-0"
            title="Send message"
          >
            <Send className="w-4 h-4" />
          </Button>
        </form>
      </div>

      {/* Group Settings Modal */}
      {showGroupModal && (
        <GroupSettingsModal
          isOpen={showGroupModal}
          onClose={() => setShowGroupModal(false)}
          conversation={activeConversation}
        />
      )}
    </div>
  );
};

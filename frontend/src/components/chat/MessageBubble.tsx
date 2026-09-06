import React, { useState } from 'react';
import { Message, User, ConversationParticipant } from '@/types';
import { formatMessageTime } from '@/utils/formatDate';
import { cn } from '@/utils/cn';
import { Check, CheckCheck, FileText, Download, Trash2, Copy, Play, Loader2, Image as ImageIcon } from 'lucide-react';
import { messagesApi } from '@/api';
import { useChatStore } from '@/stores/chatStore';

interface MessageBubbleProps {
  message: Message;
  currentUser: User | null;
  conversationType?: 'DIRECT' | 'GROUP';
  sender?: ConversationParticipant;
}

export const MessageBubble: React.FC<MessageBubbleProps> = ({
  message,
  currentUser,
  conversationType,
  sender,
}) => {
  const isMine = currentUser ? message.senderId === currentUser.id : false;
  const [showOptions, setShowOptions] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  const { fetchMessages } = useChatStore();

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this message?')) return;
    try {
      await messagesApi.deleteMessage(message.conversationId, message.id);
      fetchMessages(message.conversationId);
    } catch (err) {
      console.error('Failed to delete message:', err);
    }
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(message.content);
    setShowOptions(false);
  };

  if (message.deleted) {
    return (
      <div className={cn('flex my-1', isMine ? 'justify-end' : 'justify-start')}>
        <div className="px-4 py-2 rounded-2xl bg-slate-900/60 border border-slate-800 text-xs italic text-slate-500 max-w-sm select-none">
          This message was deleted
        </div>
      </div>
    );
  }

  const renderStatusTicks = () => {
    if (!isMine) return null;

    if (message.status === 'SEEN') {
      return <CheckCheck className="w-3.5 h-3.5 text-cyan-400 inline ml-1" title="Seen" />;
    }
    if (message.status === 'DELIVERED') {
      return <CheckCheck className="w-3.5 h-3.5 text-slate-300 inline ml-1" title="Delivered" />;
    }
    return <Check className="w-3.5 h-3.5 text-slate-400 inline ml-1" title="Sent" />;
  };

  return (
    <div
      className={cn('group flex flex-col my-1.5 relative', isMine ? 'items-end' : 'items-start')}
      onMouseEnter={() => setShowOptions(true)}
      onMouseLeave={() => setShowOptions(false)}
    >
      <div className="flex items-end gap-1.5 max-w-[85%] sm:max-w-[70%]">
        {/* Message Bubble Container */}
        <div
          className={cn(
            'relative px-4 py-2.5 rounded-2xl shadow-sm text-sm break-words transition-all',
            isMine
              ? 'bg-indigo-600 text-white rounded-br-xs shadow-indigo-600/10'
              : 'bg-slate-800/90 text-slate-100 border border-slate-700/60 rounded-bl-xs'
          )}
        >
          {/* Sender in group conversations */}
          {conversationType === 'GROUP' && !isMine && (
            <div className="text-xs font-semibold text-indigo-400 mb-1 flex items-center gap-1.5 flex-wrap">
              <span>
                {sender?.displayName || (sender?.username ? `@${sender.username}` : `User ${message.senderId.slice(-6)}`)}
              </span>
              {sender?.displayName && sender?.username && (
                <span className="text-[10px] text-slate-400 font-normal">
                  @{sender.username}
                </span>
              )}
            </div>
          )}

          {/* Media Attachments */}
          {message.attachments && message.attachments.length > 0 && (
            <div className="space-y-2 mb-2">
              {message.attachments.map((att, idx) => {
                const isImage =
                  att.contentType?.startsWith('image/') ||
                  /\.(jpe?g|png|gif|webp)$/i.test(att.url || att.originalFilename || '');
                const isVideo =
                  att.contentType?.startsWith('video/') ||
                  /\.(mp4|webm)$/i.test(att.url || att.originalFilename || '');
                const isAudio =
                  att.contentType?.startsWith('audio/') ||
                  /\.(mp3|wav|ogg)$/i.test(att.url || att.originalFilename || '');

                if (isImage) {
                  return (
                    <div
                      key={att.attachmentId || att.storageKey || idx}
                      className="relative rounded-xl overflow-hidden cursor-pointer group/img max-w-full"
                    >
                      <img
                        src={att.url}
                        alt={att.originalFilename || 'Image attachment'}
                        className="max-h-72 w-full object-cover rounded-xl hover:scale-[1.02] transition-transform duration-200"
                        onClick={() => setSelectedImage(att.url)}
                      />
                    </div>
                  );
                }

                if (isVideo) {
                  return (
                    <div key={att.attachmentId || att.storageKey || idx} className="rounded-xl overflow-hidden bg-black/40">
                      <video src={att.url} controls className="max-h-72 w-full rounded-xl" />
                    </div>
                  );
                }

                if (isAudio) {
                  return (
                    <div key={att.attachmentId || att.storageKey || idx} className="p-2 bg-slate-900/60 rounded-xl">
                      <audio src={att.url} controls className="w-full h-8" />
                    </div>
                  );
                }

                return (
                  <a
                    key={att.attachmentId || att.storageKey || idx}
                    href={att.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-2.5 bg-slate-900/60 hover:bg-slate-900 border border-slate-700/50 rounded-xl text-xs transition-colors"
                  >
                    <div className="p-2 bg-indigo-500/20 text-indigo-400 rounded-lg">
                      <FileText className="w-4 h-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="font-medium text-slate-200 truncate">{att.originalFilename}</div>
                      <div className="text-[10px] text-slate-400">
                        {att.fileSize ? `${(att.fileSize / 1024).toFixed(1)} KB` : 'Attachment'}
                      </div>
                    </div>
                    <Download className="w-4 h-4 text-slate-400 hover:text-white" />
                  </a>
                );
              })}
            </div>
          )}

          {/* Pending attachment loading state */}
          {(!message.attachments || message.attachments.length === 0) &&
            message.content?.startsWith('Sent an attachment:') && (
              <div className="flex items-center gap-2 p-2.5 rounded-xl bg-black/20 border border-white/10 mb-1.5">
                <Loader2 className="w-4 h-4 text-indigo-300 animate-spin flex-shrink-0" />
                <span className="text-xs text-indigo-100 font-medium truncate">
                  {message.content.replace('Sent an attachment: ', 'Uploading ')}
                </span>
              </div>
          )}

          {/* Text Content: only show if not an attachment placeholder */}
          {message.content && !message.content.startsWith('Sent an attachment:') && (
            <p className="whitespace-pre-wrap leading-relaxed select-text">{message.content}</p>
          )}

          {/* Timestamp & status ticks */}
          <div
            className={cn(
              'flex items-center justify-end gap-1 mt-1 text-[10px]',
              isMine ? 'text-indigo-200' : 'text-slate-400'
            )}
          >
            {message.edited && <span className="italic mr-0.5">(edited)</span>}
            <span>{formatMessageTime(message.createdAt)}</span>
            {renderStatusTicks()}
          </div>
        </div>

        {/* Action button on hover */}
        {showOptions && (
          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={handleCopy}
              className="p-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-slate-400 hover:text-slate-200 border border-slate-700 text-xs shadow-md"
              title="Copy"
            >
              <Copy className="w-3.5 h-3.5" />
            </button>
            {isMine && (
              <button
                onClick={handleDelete}
                className="p-1 rounded-lg bg-slate-800 hover:bg-rose-600/20 text-slate-400 hover:text-rose-400 border border-slate-700 text-xs shadow-md"
                title="Delete"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        )}
      </div>

      {/* Fullscreen Image Preview Modal */}
      {selectedImage && (
        <div
          className="fixed inset-0 z-50 bg-black/90 backdrop-blur-md flex items-center justify-center p-4"
          onClick={() => setSelectedImage(null)}
        >
          <img
            src={selectedImage}
            alt="Preview"
            className="max-w-full max-h-[90vh] object-contain rounded-xl shadow-2xl"
          />
        </div>
      )}
    </div>
  );
};

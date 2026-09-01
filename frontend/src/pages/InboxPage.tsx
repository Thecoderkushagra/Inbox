import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { ConversationFeed } from '@/components/chat/ConversationFeed';
import { ChatCanvas } from '@/components/chat/ChatCanvas';
import { ConversationInspector } from '@/components/chat/ConversationInspector';
import { useIsMobile } from '@/utils/useMediaQuery';
import { useChatStore } from '@/stores/chatStore';

export const InboxPage: React.FC = () => {
  const { conversationId } = useParams<{ conversationId?: string }>();
  const isMobile = useIsMobile();
  const { conversations } = useChatStore();

  const [isInspectorOpen, setIsInspectorOpen] = useState(false);

  const activeConversation = conversations.find((c) => c.id === conversationId);

  // On Mobile:
  // If a conversation is selected (conversationId exists in URL), show only ChatCanvas (with Back button)
  // If no conversation is selected, show only ConversationFeed
  if (isMobile) {
    if (conversationId && activeConversation) {
      return (
        <div className="h-full w-full flex flex-col overflow-hidden">
          <ChatCanvas
            showMobileBack={true}
            onToggleInspector={() => setIsInspectorOpen(!isInspectorOpen)}
            isInspectorOpen={isInspectorOpen}
          />
          {isInspectorOpen && (
            <div className="fixed inset-0 z-50 bg-slate-950 flex flex-col">
              <ConversationInspector
                conversation={activeConversation}
                onClose={() => setIsInspectorOpen(false)}
              />
            </div>
          )}
        </div>
      );
    }
    return (
      <div className="h-full w-full flex flex-col overflow-hidden pb-16">
        <ConversationFeed />
      </div>
    );
  }

  // On Desktop:
  // 3-Pane / 2-Pane: Middle Rail (Feed) + Right Canvas (Active Chat) + Right Inspector (if toggled)
  return (
    <div className="h-full w-full flex overflow-hidden">
      <ConversationFeed />
      <div className="flex-1 flex overflow-hidden">
        <ChatCanvas
          onToggleInspector={() => setIsInspectorOpen(!isInspectorOpen)}
          isInspectorOpen={isInspectorOpen}
        />
        {isInspectorOpen && activeConversation && (
          <ConversationInspector
            conversation={activeConversation}
            onClose={() => setIsInspectorOpen(false)}
          />
        )}
      </div>
    </div>
  );
};

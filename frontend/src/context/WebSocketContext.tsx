import React, { createContext, useContext, useEffect, useRef, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuthStore } from '@/stores/authStore';
import { useChatStore } from '@/stores/chatStore';
import { usePresenceStore } from '@/stores/presenceStore';
import { useNotificationStore } from '@/stores/notificationStore';
import { getAccessTokenCookie } from '@/utils/cookies';
import { Message, MessageStatus, NotificationResponse, PresenceResponse, ReadReceiptResponse } from '@/types';

interface WebSocketContextType {
  isConnected: boolean;
  sendMessage: (conversationId: string, content: string) => void;
}

const WebSocketContext = createContext<WebSocketContextType | null>(null);

export const WebSocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isConnected, setIsConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  const { user, isAuthenticated } = useAuthStore();
  const { receiveMessage, addMediaAttachment, updateMessageStatus, fetchConversations } = useChatStore();
  const { setPresence } = usePresenceStore();
  const { addNotification } = useNotificationStore();

  useEffect(() => {
    if (!isAuthenticated || !user) {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        setIsConnected(false);
      }
      return;
    }

    const getToken = () => getAccessTokenCookie() || localStorage.getItem('access_token') || '';
    
    // Resolve SockJS HTTP/HTTPS URL from environment
    let rawWs =
      import.meta.env.VITE_WS_BASE_URL ||
      import.meta.env.VITE_API_BASE_URL ||
      import.meta.env.VITE_WS_URL ||
      '/ws';

    if (rawWs.startsWith('wss://')) {
      rawWs = rawWs.replace('wss://', 'https://');
    } else if (rawWs.startsWith('ws://')) {
      rawWs = rawWs.replace('ws://', 'http://');
    }

    if (rawWs.startsWith('http') && !rawWs.endsWith('/ws')) {
      rawWs = `${rawWs}/ws`;
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(rawWs),
      beforeConnect: () => {
        const currentToken = getToken();
        client.connectHeaders = {
          Authorization: currentToken ? `Bearer ${currentToken}` : '',
        };
      },
      connectHeaders: {
        Authorization: getToken() ? `Bearer ${getToken()}` : '',
      },
      debug: () => {},
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      setIsConnected(true);

      // 1. Subscribe to Global Chat topic
      client.subscribe('/topic/chat', (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          const msg: Message = payload.data || payload;
          receiveMessage(msg);
        } catch (err) {
          console.error('Error parsing chat message:', err);
        }
      });

      // 2. Subscribe to Conversations topic (new conversations created/updated)
      client.subscribe('/topic/conversations', () => {
        try {
          fetchConversations();
        } catch (err) {
          console.error('Error handling conversation update:', err);
        }
      });

      // 3. Subscribe to Presence topic
      client.subscribe('/topic/presence', (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          const presence: PresenceResponse = payload.data || payload;
          if (presence && presence.userId) {
            setPresence(presence.userId, presence.status, presence.lastSeen);
          }
        } catch (err) {
          console.error('Error parsing presence update:', err);
        }
      });

      // 4. Subscribe to Read Receipts topic
      client.subscribe('/topic/read-receipts', (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          const receipt: ReadReceiptResponse = payload.data || payload;
          if (receipt && receipt.messageId) {
            const status: MessageStatus = receipt.seenAt ? 'SEEN' : 'DELIVERED';
            updateMessageStatus(receipt.messageId, status);
          }
        } catch (err) {
          console.error('Error parsing read receipt:', err);
        }
      });

      // 5. Subscribe to Private User Notifications
      client.subscribe('/user/queue/notifications', (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          const notif: NotificationResponse = payload.data || payload;
          addNotification(notif);
        } catch (err) {
          console.error('Error parsing notification:', err);
        }
      });

      // 6. Subscribe to Media topic (incoming attachments in real-time)
      client.subscribe('/topic/media', (message: IMessage) => {
        try {
          const payload = JSON.parse(message.body);
          const media: MediaAttachment = payload.data || payload;
          if (media) {
            addMediaAttachment(media);
          }
        } catch (err) {
          console.error('Error parsing media event:', err);
        }
      });
    };

    client.onDisconnect = () => {
      setIsConnected(false);
    };

    client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
        setIsConnected(false);
      }
    };
  }, [isAuthenticated, user?.id]);

  const sendMessage = (conversationId: string, content: string) => {
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({ conversationId, content }),
      });
    }
  };

  return (
    <WebSocketContext.Provider value={{ isConnected, sendMessage }}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (!context) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};

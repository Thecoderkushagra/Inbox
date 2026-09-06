/**
 * STOMP WebSocket Client Test Helper
 * Wraps @stomp/stompjs Client with Node.js ws transport for automated socket testing.
 */

import { Client, IMessage, StompHeaders } from '@stomp/stompjs';
import WebSocket from 'ws';
import { WS_URL } from './db_seed';

// Resolve raw WebSocket URL from HTTP/SockJS endpoint (ws://localhost:8080/ws/websocket)
export function getRawWebSocketUrl(baseWsUrl: string = WS_URL): string {
  let url = baseWsUrl;
  if (url.startsWith('https://')) {
    url = url.replace('https://', 'wss://');
  } else if (url.startsWith('http://')) {
    url = url.replace('http://', 'ws://');
  }
  if (!url.endsWith('/websocket')) {
    url = `${url.replace(/\/$/, '')}/websocket`;
  }
  return url;
}

/**
 * Creates and activates a test STOMP client. Returns a promise that resolves on CONNECTED frame.
 */
export function connectTestStompClient(token?: string, endpoint: string = getRawWebSocketUrl()): Promise<Client> {
  return new Promise((resolve, reject) => {
    const connectHeaders: StompHeaders = {};
    if (token) {
      connectHeaders['Authorization'] = `Bearer ${token}`;
    }

    const client = new Client({
      webSocketFactory: () => new WebSocket(endpoint) as any,
      connectHeaders,
      debug: () => {},
      reconnectDelay: 0,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 0,
    });

    const timeout = setTimeout(() => {
      client.deactivate();
      reject(new Error('STOMP Connection timeout (5000ms exceeded)'));
    }, 8000);

    client.onConnect = () => {
      clearTimeout(timeout);
      resolve(client);
    };

    client.onStompError = (frame) => {
      clearTimeout(timeout);
      reject(new Error(`STOMP Error: ${frame.headers['message'] || 'Connection failed'}`));
    };

    client.onWebSocketError = (event) => {
      clearTimeout(timeout);
      reject(new Error(`WebSocket Transport Error: ${JSON.stringify(event)}`));
    };

    client.activate();
  });
}

/**
 * Awaits a single STOMP message on a destination topic with a configurable timeout.
 */
export function awaitStompMessage<T = any>(client: Client, destination: string, timeoutMs: number = 5000): Promise<T> {
  return new Promise((resolve, reject) => {
    let sub: any;
    const timer = setTimeout(() => {
      if (sub) sub.unsubscribe();
      reject(new Error(`Timeout waiting for message on ${destination} after ${timeoutMs}ms`));
    }, timeoutMs);

    sub = client.subscribe(destination, (message: IMessage) => {
      clearTimeout(timer);
      try {
        const payload = JSON.parse(message.body);
        const data = payload.data !== undefined ? payload.data : payload;
        sub.unsubscribe();
        resolve(data);
      } catch (err) {
        sub.unsubscribe();
        reject(err);
      }
    });
  });
}

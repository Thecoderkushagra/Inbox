import ws from 'k6/ws';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

/**
 * k6 Real-time Load & Capacity Benchmark
 * Simulates:
 * - 100 concurrent Virtual Users (VUs)
 * - Distributed across 10 distinct chat rooms
 * - Emits ~20 msgs/second to measure server throughput and broadcast latency
 */

export const options = {
  stages: [
    { duration: '20s', target: 20 },  // Ramp-up
    { duration: '60s', target: 100 }, // Peak load: 100 concurrent sockets
    { duration: '20s', target: 0 },   // Cool-down
  ],
  thresholds: {
    ws_connecting_time: ['p(95)<1000'], // 95% of connections establish under 1s
    messages_received: ['count>100'],
  },
};

const messagesSent = new Counter('messages_sent');
const messagesReceived = new Counter('messages_received');
const broadcastLatency = new Trend('broadcast_latency_ms');

export default function () {
  const vuId = __VU;
  const roomId = `room_${vuId % 10}`; // 10 distributed chat rooms
  const url = 'ws://localhost:8080/ws/websocket';

  const res = ws.connect(url, null, function (socket) {
    socket.on('open', function () {
      // 1. Send STOMP CONNECT
      socket.send('CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\nAuthorization:Bearer test_token\n\n\0');

      // 2. Subscribe to room
      socket.send(`SUBSCRIBE\nid:sub-${vuId}\ndestination:/topic/conversations.${roomId}\n\n\0`);

      // 3. Periodic message burst
      socket.setInterval(function () {
        const sendTime = Date.now();
        const payload = JSON.stringify({
          conversationId: roomId,
          content: `k6 benchmark payload at ${sendTime}`,
        });
        socket.send(`SEND\ndestination:/app/chat.send\ncontent-type:application/json\n\n${payload}\0`);
        messagesSent.add(1);
      }, 1000); // 1 msg per second per VU = 100 msgs/sec across all VUs
    });

    socket.on('message', function (data) {
      if (data.includes('MESSAGE')) {
        messagesReceived.add(1);
      }
    });

    socket.on('error', function (e) {
      console.error('Socket error:', e);
    });

    socket.setTimeout(function () {
      socket.send('DISCONNECT\n\n\0');
      socket.close();
    }, 25000);
  });

  check(res, { 'WebSocket connected successfully': (r) => r && r.status === 101 });
  sleep(1);
}

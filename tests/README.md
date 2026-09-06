# Inbox Reliability & Multi-Tiered Test Suite

> **End-to-end, multi-tiered test framework and reliability harness for the Inbox messaging platform.**
> Covers unit, API integration, STOMP real-time WebSocket, database persistence, multi-user Playwright E2E, chaos/resilience, security penetration, and load testing.

---

## 🔍 Step 1: Architectural & Runtime Diagnostics

During deep inspection of the repository (both backend and frontend), the following critical vulnerabilities and failure modes were diagnosed:

| # | Vulnerability / Bug | Impact | Root Cause & Location |
|---|---|---|---|
| **1** | **Global Topic Privacy Leak** | **CRITICAL** | [`MessageService.java`](file:///home/kushagra/Documents/Inbox/backend/src/main/java/com/messaging/backend/messaging/service/MessageService.java#L136) broadcasts every message to `GLOBAL_CHAT_TOPIC` (`/topic/chat`). Every connected client in [`WebSocketContext.tsx`](file:///home/kushagra/Documents/Inbox/frontend/src/context/WebSocketContext.tsx#L70) subscribes to `/topic/chat`. Any user receives messages from conversations they are not a member of. |
| **2** | **WebSocket Subscription IDOR** | **CRITICAL** | [`JwtChannelInterceptor.java`](file:///home/kushagra/Documents/Inbox/backend/src/main/java/com/messaging/backend/websocket/security/JwtChannelInterceptor.java#L40) validates JWT strictly on `CONNECT`. There is **zero destination authorization on `SUBSCRIBE`**. Any authenticated user can subscribe to `/topic/conversations.{groupId}` and eavesdrop on private groups. |
| **3** | **Group Chat Real-Time Desync** | **HIGH** | When a group is created or updated, [`GroupService.java`](file:///home/kushagra/Documents/Inbox/backend/src/main/java/com/messaging/backend/groups/service/GroupService.java#L307) broadcasts to `/topic/groups`. However, frontend [`WebSocketContext.tsx`](file:///home/kushagra/Documents/Inbox/frontend/src/context/WebSocketContext.tsx) listens to `/topic/conversations`. Members added to groups never receive real-time UI updates until manual refresh. |
| **4** | **Missing MongoDB Document Versioning** | **HIGH** | [`Conversation.java`](file:///home/kushagra/Documents/Inbox/backend/src/main/java/com/messaging/backend/messaging/entity/Conversation.java#L42) embeds participants in a `Set<ConversationParticipant>`. Concurrent additions without `@Version` or locking will cause last-write-wins overwriting of members. |
| **5** | **Double Ingestion on HTTP + Socket** | **MEDIUM** | [`chatStore.ts`](file:///home/kushagra/Documents/Inbox/frontend/src/stores/chatStore.ts#L80) calls `receiveMessage(msg)` immediately on HTTP POST response, and then receives the same message again over `/topic/chat` WebSocket, triggering duplicate store updates. |

---

## 🏗️ Test Suite Hierarchy

```text
/home/kushagra/Documents/Inbox/tests/
├── unit/
│   ├── backend/
│   │   ├── group_service.test.ts      # Group creation, roles (OWNER, ADMIN, MEMBER), boundaries
│   │   ├── message_service.test.ts    # Message length limits (5000), blank check, sender auth
│   │   └── input_sanitizer.test.ts    # @NoHtml validator, script stripping, injection tests
│   └── frontend/
│       ├── chat_store.test.ts         # Zustand store actions, deduplication, chronological sorting
│       └── message_bubble.test.ts     # Status indicators (SENT, DELIVERED, SEEN), time formatting
├── integration/
│   ├── api/
│   │   ├── auth_api.test.ts           # Registration, login, 401/409 error handling
│   │   └── group_api.test.ts          # Group creation, rename, add/remove member, leave
│   ├── socket/
│   │   ├── stomp_lifecycle.test.ts    # STOMP handshake, JWT validation, disconnect
│   │   ├── group_broadcast.test.ts    # 3-user group chat broadcast, delivery receipt
│   │   └── room_leave_socket.test.ts  # Post-removal message isolation
│   └── db/
│       └── mongo_persistence.test.ts  # Chronological pagination, lastMessageAt updates
├── e2e/
│   ├── auth.spec.ts                   # Registration and login flow via UI
│   ├── direct_chat.spec.ts            # 2-user real-time direct messaging between 2 browser contexts
│   └── group_chat.spec.ts             # 3+ concurrent users in the same room
├── load/
│   ├── socket_flood.yml               # Artillery: 100 concurrent sockets, message burst
│   └── room_broadcast_scale.js        # k6: 10 rooms, 100 VUs, 20 msg/sec per room
├── chaos/
│   ├── connection_drop.test.ts        # Rapid reconnect loops, subscription recovery
│   ├── out_of_order_acks.test.ts      # Network delay, out-of-order message reconciliation
│   └── race_conditions.test.ts        # Simultaneous message dispatch from 3 users
├── security/
│   ├── idor_chat_rooms.test.ts        # Unauthorized subscription/posting to non-member groups
│   ├── xss_injection.test.ts          # Malicious payloads: <script>, <img> onerror, javascript:
│   └── rate_limiting.test.ts          # Exceeding burst limits returns HTTP 429
├── fixtures/
│   ├── auth_fixtures.ts               # User A, B, C, Eve, mock JWT generator
│   ├── conversation_fixtures.ts       # Group payloads, participant roles
│   ├── message_fixtures.ts            # Valid, 5000-char boundary, malformed, and XSS vectors
│   ├── socket_helper.ts               # Node.js STOMP WebSocket client wrapper
│   └── db_seed.ts                     # API-based dynamic test user and room seeding
├── scripts/
│   ├── run_all_tests.sh               # Master test runner
│   ├── run_unit.sh                    # Tier 1 unit test runner
│   ├── run_integration.sh             # Tier 2 integration test runner
│   ├── run_chaos.sh                   # Tier 3 chaos test runner
│   ├── run_security.sh                # Tier 4 security test runner
│   ├── run_e2e.sh                     # Tier 5 Playwright E2E runner
│   └── run_load.sh                    # Tier 6 Artillery / k6 load runner
├── package.json
├── tsconfig.json
├── vitest.config.ts
├── playwright.config.ts
└── .env.test.example
```

---

## 🚀 Setup & Execution Guide

### 1. Prerequisites & Dependencies
Inside `/home/kushagra/Documents/Inbox/tests/`:

```bash
cd /home/kushagra/Documents/Inbox/tests
npm install
npx playwright install --with-deps chromium
```

### 2. Environment Configuration
Copy `.env.test.example` to `.env.test` or configure target URLs:

```bash
cp .env.test.example .env.test
```

Default target addresses:
* `TEST_BACKEND_URL=http://localhost:8080`
* `TEST_FRONTEND_URL=http://localhost:5173`
* `TEST_WS_URL=http://localhost:8080/ws`

---

## 🧪 Running Tests by Tier

### Master Orchestrator (All Tests)
```bash
./scripts/run_all_tests.sh
```

### Tier 1: Unit Tests (Backend Logic & Frontend Stores)
```bash
./scripts/run_unit.sh
# or
npm run test:unit
```

### Tier 2: Integration & Real-Time STOMP Sockets
```bash
./scripts/run_integration.sh
# or
npm run test:integration
```

### Tier 3: Chaos & Resilience
```bash
./scripts/run_chaos.sh
# or
npm run test:chaos
```

### Tier 4: Security & Penetration (IDOR, XSS, Rate Limiting)
```bash
./scripts/run_security.sh
# or
npm run test:security
```

### Tier 5: Multi-Browser End-to-End (Playwright)
```bash
./scripts/run_e2e.sh
# or
npx playwright test
```

### Tier 6: Load & Capacity Benchmarks
```bash
# Using Artillery
npx artillery run load/socket_flood.yml

# Using k6
k6 run load/room_broadcast_scale.js
```

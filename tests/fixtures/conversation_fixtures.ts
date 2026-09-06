/**
 * Conversation and Room Fixtures
 * Standardized payloads for group creation, participant roles, and conversation models.
 */

export interface TestParticipant {
  userId: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER';
  status: 'ACTIVE' | 'LEFT' | 'MUTED';
  joinedAt: string;
}

export interface TestConversation {
  id: string;
  type: 'DIRECT' | 'GROUP';
  title?: string;
  description?: string;
  participants: TestParticipant[];
  participantUserIds: string[];
  lastMessageAt: string;
  createdAt: string;
  archived: boolean;
}

export const VALID_GROUP_PAYLOAD = {
  title: 'Engineering Reliability Core',
  description: 'Mission critical incident management and real-time reliability channel.',
  initialMemberIds: ['65e9b1a10000000000000002', '65e9b1a10000000000000003'],
};

export const LARGE_GROUP_TITLE = 'A'.repeat(101); // Exceeds 100 char limit
export const LARGE_GROUP_DESC = 'D'.repeat(501);  // Exceeds 500 char limit

export function createMockGroup(
  groupId: string,
  ownerId: string,
  memberIds: string[] = [],
  title: string = 'QA War Room'
): TestConversation {
  const now = new Date().toISOString();
  const participants: TestParticipant[] = [
    {
      userId: ownerId,
      role: 'OWNER',
      status: 'ACTIVE',
      joinedAt: now,
    },
    ...memberIds.map((id): TestParticipant => ({
      userId: id,
      role: 'MEMBER',
      status: 'ACTIVE',
      joinedAt: now,
    })),
  ];

  return {
    id: groupId,
    type: 'GROUP',
    title,
    description: 'A mock group room for verification tests',
    participants,
    participantUserIds: [ownerId, ...memberIds],
    lastMessageAt: now,
    createdAt: now,
    archived: false,
  };
}

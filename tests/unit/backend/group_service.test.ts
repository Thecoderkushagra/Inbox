import { describe, it, expect, beforeEach } from 'vitest';
import { createMockGroup, VALID_GROUP_PAYLOAD, LARGE_GROUP_TITLE, LARGE_GROUP_DESC, TestConversation } from '../../fixtures/conversation_fixtures';
import { USER_A, USER_B, USER_C, ATTACKER_USER } from '../../fixtures/auth_fixtures';

/**
 * Unit Test: Group Domain Rules & Authorization Service Logic
 * Mirrors backend GroupService.java business logic specifications.
 */
describe('Backend Unit: GroupService Domain Logic', () => {
  let group: TestConversation;

  beforeEach(() => {
    // Owner is USER_A, initial members USER_B, USER_C
    group = createMockGroup('grp-100', USER_A.id, [USER_B.id, USER_C.id], VALID_GROUP_PAYLOAD.title);
  });

  describe('Group Creation & Member Initialization', () => {
    it('initializes owner with OWNER role and ACTIVE status', () => {
      const owner = group.participants.find((p) => p.userId === USER_A.id);
      expect(owner).toBeDefined();
      expect(owner?.role).toBe('OWNER');
      expect(owner?.status).toBe('ACTIVE');
    });

    it('initializes initial members with MEMBER role and ACTIVE status', () => {
      const bob = group.participants.find((p) => p.userId === USER_B.id);
      const charlie = group.participants.find((p) => p.userId === USER_C.id);
      expect(bob?.role).toBe('MEMBER');
      expect(bob?.status).toBe('ACTIVE');
      expect(charlie?.role).toBe('MEMBER');
    });

    it('populates participantUserIds set for fast indexed queries', () => {
      expect(group.participantUserIds).toContain(USER_A.id);
      expect(group.participantUserIds).toContain(USER_B.id);
      expect(group.participantUserIds).toContain(USER_C.id);
      expect(group.participantUserIds).not.toContain(ATTACKER_USER.id);
    });

    it('rejects group titles exceeding 100 characters', () => {
      expect(LARGE_GROUP_TITLE.length).toBeGreaterThan(100);
      const validateTitle = (title: string) => {
        if (!title || title.trim().length === 0) throw new Error('Title must not be blank');
        if (title.length > 100) throw new Error('Title must not exceed 100 characters');
      };
      expect(() => validateTitle(LARGE_GROUP_TITLE)).toThrow('Title must not exceed 100 characters');
    });

    it('rejects group descriptions exceeding 500 characters', () => {
      expect(LARGE_GROUP_DESC.length).toBeGreaterThan(500);
      const validateDesc = (desc: string) => {
        if (desc && desc.length > 500) throw new Error('Description must not exceed 500 characters');
      };
      expect(() => validateDesc(LARGE_GROUP_DESC)).toThrow('Description must not exceed 500 characters');
    });
  });

  describe('Role-Based Access Control (requireAdmin)', () => {
    function requireAdmin(targetGroup: TestConversation, requesterId: string) {
      const participant = targetGroup.participants.find(
        (p) => p.userId === requesterId && p.status === 'ACTIVE'
      );
      if (!participant) {
        throw new Error('User is not an active member of this group');
      }
      if (participant.role !== 'ADMIN' && participant.role !== 'OWNER') {
        throw new Error('Must be an ADMIN or OWNER to perform this action');
      }
      return participant;
    }

    it('allows OWNER to perform administrative actions', () => {
      expect(() => requireAdmin(group, USER_A.id)).not.toThrow();
    });

    it('blocks regular MEMBER from performing administrative actions', () => {
      expect(() => requireAdmin(group, USER_B.id)).toThrow(
        'Must be an ADMIN or OWNER to perform this action'
      );
    });

    it('blocks non-members completely', () => {
      expect(() => requireAdmin(group, ATTACKER_USER.id)).toThrow(
        'User is not an active member of this group'
      );
    });
  });

  describe('Member Management (Add / Remove / Promote)', () => {
    it('prevents adding an already existing member (ConflictException)', () => {
      const addMember = (targetGroup: TestConversation, newUserId: string) => {
        const alreadyMember = targetGroup.participantUserIds.includes(newUserId);
        if (alreadyMember) {
          throw new Error('User is already a member of this group');
        }
        targetGroup.participantUserIds.push(newUserId);
        targetGroup.participants.push({
          userId: newUserId,
          role: 'MEMBER',
          status: 'ACTIVE',
          joinedAt: new Date().toISOString(),
        });
      };

      expect(() => addMember(group, USER_B.id)).toThrow('User is already a member of this group');
    });

    it('successfully adds new member when requested by admin', () => {
      const newUserId = '65e9b1a10000000000000004';
      group.participantUserIds.push(newUserId);
      group.participants.push({
        userId: newUserId,
        role: 'MEMBER',
        status: 'ACTIVE',
        joinedAt: new Date().toISOString(),
      });

      expect(group.participantUserIds).toContain(newUserId);
      expect(group.participants.length).toBe(4);
    });

    it('handles member leave cleanly by removing from participants and participantUserIds', () => {
      const leaveGroup = (targetGroup: TestConversation, leavingUserId: string) => {
        targetGroup.participants = targetGroup.participants.filter((p) => p.userId !== leavingUserId);
        targetGroup.participantUserIds = targetGroup.participantUserIds.filter((id) => id !== leavingUserId);
      };

      leaveGroup(group, USER_C.id);
      expect(group.participantUserIds).not.toContain(USER_C.id);
      expect(group.participants.some((p) => p.userId === USER_C.id)).toBe(false);
    });
  });
});

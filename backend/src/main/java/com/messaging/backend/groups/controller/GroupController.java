package com.messaging.backend.groups.controller;

import com.messaging.backend.auth.security.AuthenticatedUser;
import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.groups.dto.request.AddGroupMemberRequest;
import com.messaging.backend.groups.dto.request.CreateGroupRequest;
import com.messaging.backend.groups.dto.request.DemoteAdminRequest;
import com.messaging.backend.groups.dto.request.PromoteAdminRequest;
import com.messaging.backend.groups.dto.request.RenameGroupRequest;
import com.messaging.backend.groups.dto.request.UpdateGroupDescriptionRequest;
import com.messaging.backend.groups.dto.response.GroupMemberResponse;
import com.messaging.backend.groups.dto.response.GroupResponse;
import com.messaging.backend.groups.mapper.GroupMapper;
import com.messaging.backend.groups.service.GroupService;
import com.messaging.backend.messaging.entity.Conversation;
import com.messaging.backend.messaging.entity.ConversationParticipant;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    public GroupController(GroupService groupService, GroupMapper groupMapper) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<List<GroupResponse>>> getMyGroups(
            @AuthenticationPrincipal AuthenticatedUser currentUser) {

        List<Conversation> groups = groupService.getMyGroups(currentUser.getId());
        List<GroupResponse> response = groupMapper.toGroupResponseList(groups);
        return ResponseEntity.ok(SuccessResponse.success("Groups retrieved successfully", response));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<SuccessResponse<GroupResponse>> getGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId) {

        Conversation group = groupService.getGroup(groupId, currentUser.getId());
        GroupResponse response = groupMapper.toGroupResponse(group);
        return ResponseEntity.ok(SuccessResponse.success("Group retrieved successfully", response));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<SuccessResponse<List<GroupMemberResponse>>> getMembers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId) {

        List<ConversationParticipant> members = groupService.getMembers(groupId, currentUser.getId());
        List<GroupMemberResponse> response = groupMapper.toGroupMemberResponseList(members);
        return ResponseEntity.ok(SuccessResponse.success("Group members retrieved successfully", response));
    }

    @PostMapping
    public ResponseEntity<SuccessResponse<GroupResponse>> createGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateGroupRequest request) {

        Conversation group = groupService.createGroup(
                currentUser.getId(),
                request.title(),
                request.description(),
                request.initialMemberIds()
        );
        GroupResponse response = groupMapper.toGroupResponse(group);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Group created successfully", response));
    }

    @PatchMapping("/{groupId}/rename")
    public ResponseEntity<SuccessResponse<GroupResponse>> renameGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId,
            @Valid @RequestBody RenameGroupRequest request) {

        Conversation group = groupService.renameGroup(groupId, currentUser.getId(), request.title());
        GroupResponse response = groupMapper.toGroupResponse(group);
        return ResponseEntity.ok(SuccessResponse.success("Group renamed successfully", response));
    }

    @PatchMapping("/{groupId}/description")
    public ResponseEntity<SuccessResponse<GroupResponse>> updateDescription(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId,
            @Valid @RequestBody UpdateGroupDescriptionRequest request) {

        Conversation group = groupService.updateGroupDescription(groupId, currentUser.getId(), request.description());
        GroupResponse response = groupMapper.toGroupResponse(group);
        return ResponseEntity.ok(SuccessResponse.success("Group description updated successfully", response));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<SuccessResponse<GroupMemberResponse>> addMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {

        ConversationParticipant member = groupService.addMember(groupId, currentUser.getId(), request.userId());
        GroupMemberResponse response = groupMapper.toGroupMemberResponse(member);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success("Member added successfully", response));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<SuccessResponse<Void>> removeMember(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId,
            @PathVariable String userId) {

        groupService.removeMember(groupId, currentUser.getId(), userId);
        return ResponseEntity.ok(SuccessResponse.success("Member removed successfully", null));
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<SuccessResponse<Void>> leaveGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId) {

        groupService.leaveGroup(groupId, currentUser.getId());
        return ResponseEntity.ok(SuccessResponse.success("Left group successfully", null));
    }

    @PostMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<SuccessResponse<GroupMemberResponse>> promoteAdmin(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId,
            @PathVariable String userId) {

        ConversationParticipant member = groupService.promoteAdmin(groupId, currentUser.getId(), userId);
        GroupMemberResponse response = groupMapper.toGroupMemberResponse(member);
        return ResponseEntity.ok(SuccessResponse.success("Admin promoted successfully", response));
    }

    @DeleteMapping("/{groupId}/admins/{userId}")
    public ResponseEntity<SuccessResponse<GroupMemberResponse>> demoteAdmin(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId,
            @PathVariable String userId) {

        ConversationParticipant member = groupService.demoteAdmin(groupId, currentUser.getId(), userId);
        GroupMemberResponse response = groupMapper.toGroupMemberResponse(member);
        return ResponseEntity.ok(SuccessResponse.success("Admin demoted successfully", response));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<SuccessResponse<Void>> deleteGroup(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable String groupId) {

        groupService.deleteGroup(groupId, currentUser.getId());
        return ResponseEntity.ok(SuccessResponse.success("Group deleted successfully", null));
    }
}

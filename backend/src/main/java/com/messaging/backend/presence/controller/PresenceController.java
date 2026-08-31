package com.messaging.backend.presence.controller;

import com.messaging.backend.common.dto.response.SuccessResponse;
import com.messaging.backend.presence.dto.PresenceResponse;
import com.messaging.backend.presence.service.PresenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes presence status to external clients via REST.
 */
@RestController
@RequestMapping("/api/v1/presence")
public class PresenceController {

    private final PresenceService presenceService;

    public PresenceController(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    /**
     * Retrieves a user's current presence status.
     * 
     * @param userId the ID of the user to query
     * @return 200 OK with the mapped presence DTO
     */
    @GetMapping("/{userId}")
    public ResponseEntity<SuccessResponse<PresenceResponse>> getPresence(@PathVariable String userId) {
        PresenceResponse response = presenceService.getPresence(userId);
        return ResponseEntity.ok(SuccessResponse.success(response));
    }
}

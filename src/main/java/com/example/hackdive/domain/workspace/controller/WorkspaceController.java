package com.example.hackdive.domain.workspace.controller;

import com.example.hackdive.domain.workspace.service.WorkspaceService;
import com.example.hackdive.global.common.SuccessResponse;
import lombok.AllArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspace")
@AllArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    @PostMapping("/generation/{userId}")
    public ResponseEntity<SuccessResponse<?>> generateWorkspace(@PathVariable("userId") Long userId) {
        Long id = workspaceService.generateWorkspace(userId);
        return SuccessResponse.ok(id);
    }
}

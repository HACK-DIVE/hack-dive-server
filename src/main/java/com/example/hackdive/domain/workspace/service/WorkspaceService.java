package com.example.hackdive.domain.workspace.service;

import com.example.hackdive.domain.user.entity.User;
import com.example.hackdive.domain.user.repository.UserRepository;
import com.example.hackdive.domain.workspace.entity.Workspace;
import com.example.hackdive.domain.workspace.repository.WorkspaceRepository;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    public Long generateWorkspace(Long userId) {
        Optional<User> safeUser = userRepository.findById(userId);
        if(safeUser.isEmpty()) throw new RuntimeException("No User id " + userId);

        Workspace workspace = workspaceRepository.save(
                Workspace.builder()
                        .user(safeUser.get())
                        .createdAt(LocalDateTime.now())
                        .editedAt(LocalDateTime.now())
                        .workspaceName("workspace")
                        .build()
        );

        return workspace.getId();
    }
}

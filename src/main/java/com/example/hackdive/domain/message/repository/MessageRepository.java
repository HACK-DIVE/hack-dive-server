package com.example.hackdive.domain.message.repository;

import com.example.hackdive.domain.message.entity.Message;
import com.example.hackdive.domain.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByWorkspaceOrderByCreatedAtDesc(Workspace workspace);
}

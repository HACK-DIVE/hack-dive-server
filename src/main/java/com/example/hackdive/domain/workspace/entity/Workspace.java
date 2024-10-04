package com.example.hackdive.domain.workspace.entity;

import com.example.hackdive.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Builder
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Workspace {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="workspace_id", nullable = false)
    private Long id;

    @Column(name="workspace_name", nullable = false)
    private String workspaceName;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private LocalDateTime editedAt;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

}

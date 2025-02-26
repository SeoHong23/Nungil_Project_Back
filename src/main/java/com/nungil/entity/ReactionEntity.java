package com.nungil.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "user_video_reactions", uniqueConstraints = @UniqueConstraint(columnNames = {"video_id", "user_id"}))
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_id", nullable = false)
    private String videoId;  // MongoDB 비디오 ID

    @Column(name = "user_id", nullable = false)
    private Long userId;     // MySQL 유저 ID

    private int reaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;
}

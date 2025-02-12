package com.nungil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notinterested")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotInterestedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 유저 (MySQL 기준)

    @Column(name = "video_id", nullable = false)
    private String videoId;


}

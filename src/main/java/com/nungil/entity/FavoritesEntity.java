package com.nungil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 찜하기 PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user; // 유저 (MySQL 기준)

    @Column(name = "video_id", nullable = false)
    private String videoId;
}

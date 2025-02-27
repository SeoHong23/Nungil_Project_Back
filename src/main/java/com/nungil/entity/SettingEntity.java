package com.nungil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setting", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int setting_id;


    @Column(name = "user_id", nullable = false)
    private Long userId;

    private char is_alert;
}

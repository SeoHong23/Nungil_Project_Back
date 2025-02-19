package com.nungil.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.bson.types.ObjectId;

@Entity
@Table(name = "banner")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BannerEntity {
    @Id
    private ObjectId id;           // MongoDB에서 사용되는 고유 ID
    private String title;
    private String path;
}

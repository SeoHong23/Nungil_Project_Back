package com.nungil.Document;

import com.nungil.Dto.ReactionDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({                                      // 복합 인덱스
        @CompoundIndex(name = "user_video_idx",
                def = "{'userId' : 1, 'videoId': 1}",   // 정렬 : 1 (오름차순), -1 (내림차순)
                unique = true)                          // 고유 인덱스 (중복 허용 X)
})
@Document(collection = "videoReaction")
public class VideoReaction {

    @Id
    private String mongoId;

    private int objectId;

    @Indexed
    private int userId;

    @Indexed
    private String videoId;

    private String title;
    private String posterUrl;

    private Reactions reactions;


    @LastModifiedDate
    private Date updatedAt;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reactions {

        private boolean isBookmarked;
        private boolean isIgnored;
        private boolean isLike;
        private boolean isDisliked;
        private boolean isWatching;
        private boolean isWatched;
    }


    public ReactionDTO toDTO() {
        Reactions safeReactions = reactions != null ? reactions : new Reactions();
        return ReactionDTO.builder()
                .mongoId(mongoId)
                .objectId(objectId)
                .userId(userId)
                .videoId(videoId)
                .title(title)
                .posterUrl(posterUrl)
                .isBookmarked(safeReactions.isBookmarked())
                .isIgnored(safeReactions.isIgnored())
                .isLike(safeReactions.isLike())
                .isDisliked(safeReactions.isDisliked())
                .isWatching(safeReactions.isWatching())
                .isWatched(safeReactions.isWatched())
                .build();
    }
}

package com.nungil.Dto;

import com.nungil.Document.VideoReaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReactionDTO {

    private String mongoId;
    private int objectId;

    private int userId;
    private String videoId;
    private String title;
    private String posterUrl;

    private boolean isBookmarked;
    private boolean isIgnored;
    private boolean isLike;
    private boolean isDisliked;
    private boolean isWatching;
    private boolean isWatched;

    private Date updatedAt;

    public VideoReaction toDocument() {
        return VideoReaction.builder()
                .mongoId(this.mongoId)
                .objectId(this.objectId)
                .userId(this.userId)
                .videoId(this.videoId)
                .title(this.title)
                .posterUrl(this.posterUrl)
                .reactions(VideoReaction.Reactions.builder()
                        .isBookmarked(this.isBookmarked)
                        .isIgnored(this.isIgnored)
                        .isLike(this.isLike)
                        .isDisliked(this.isDisliked)
                        .isWatching(this.isWatching)
                        .isWatched(this.isWatched)
                        .build())
                .updatedAt(updatedAt)
                .build();
    }

}

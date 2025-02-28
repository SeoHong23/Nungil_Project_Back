package com.nungil.Dto;

import com.nungil.Document.VideoReaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;


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

    private Boolean isBookmarked;
    private Boolean isIgnored;
    private Boolean isLiked;
    private Boolean isDisliked;
    private Boolean isWatching;
    private Boolean isWatched;

    public VideoReaction toDocument() {
        return VideoReaction.builder()
                .id(Objects.equals(mongoId, "") ?null:this.mongoId)
                .objectId(this.objectId)
                .userId(this.userId)
                .videoId(this.videoId)
                .title(this.title)
                .posterUrl(this.posterUrl)
                .reactions(VideoReaction.Reactions.builder()
                        .isBookmarked(this.isBookmarked)
                        .isIgnored(this.isIgnored)
                        .isLiked(this.isLiked)
                        .isDisliked(this.isDisliked)
                        .isWatching(this.isWatching)
                        .isWatched(this.isWatched)
                        .build())
                .build();
    }

}

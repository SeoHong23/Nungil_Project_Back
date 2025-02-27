package com.nungil.Dto;

import lombok.Data;

import java.util.List;

@Data
public class SyncRequestDTO {
    private List<ReactionDTO> reactions;
    private List<String> deletedItems;

    public void setUserId(int userId) {
        reactions = reactions.stream().peek((dto) -> dto.setUserId(userId)).toList();
    }
}

package com.nungil.Service;


import com.nungil.Document.VideoReaction;
import com.nungil.Dto.ReactionDTO;
import com.nungil.Dto.SyncRequestDTO;
import com.nungil.Repository.Interfaces.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class ReactionService {

    private final ReactionRepository reactionRepository;

    @Transactional
    public List<ReactionDTO> syncReactions(SyncRequestDTO syncRequestDTO) {
        deleteList(syncRequestDTO.getDeletedItems());

        return syncRequestDTO.getReactions().stream()
                .map(this::saveReaction)
                .toList();
    }

    // 개별 ReactionDTO 객체 저장 또는 업데이트
    private ReactionDTO saveReaction(ReactionDTO reaction) {
        Optional<VideoReaction> existingReaction = reactionRepository.findById(reaction.getMongoId());

        VideoReaction updatedReaction = existingReaction
                .map(doc -> updateReaction(doc, reaction.toDocument().getReactions())) // 기존 객체 업데이트
                .orElseGet(() -> reactionRepository.save(reaction.toDocument())); // 새 객체 저장

        return updatedReaction.toDTO();
    }

    // 기존 객체 업데이트 (필요한 속성만 업데이트)
    private VideoReaction updateReaction(VideoReaction document, VideoReaction.Reactions reactions) {

        document.setReactions(reactions);
        // 업데이트된 ReactionDTO 객체 저장
        return reactionRepository.save(document);
    }

    private void deleteList(List<String> ids) {
        for (String id : ids) {
            deleteReaction(id);
        }
    }

    private void deleteReaction(String reactionId) {
        try {
            reactionRepository.deleteById(reactionId);
        } catch (Exception e) {
            log.error(e);
        }
    }
}

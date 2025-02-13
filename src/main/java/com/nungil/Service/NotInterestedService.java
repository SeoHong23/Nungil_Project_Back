package com.nungil.Service;

import com.nungil.Dto.NotInterestedDTO;
import com.nungil.Repository.Interfaces.NotInterestedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotInterestedService {

    private final NotInterestedRepository notInterestedRepository;

    public void addNotInterested(NotInterestedDTO notInterestedDTO) {
        notInterestedRepository.insertNotInterested(notInterestedDTO);
    }

    public void removeNotInterested(NotInterestedDTO notInterestedDTO) {
        notInterestedRepository.deleteNotInterested(notInterestedDTO);
    }

    public boolean checkNotInterested(NotInterestedDTO notInterestedDTO) {
        return notInterestedRepository.existsByUserIdAndVideoId(notInterestedDTO.getUserId(), notInterestedDTO.getVideoId());
    }

    public Long countNotInterestedByUser(Long userId) {
        return notInterestedRepository.countNotInterestedByUser(userId);
    }
}

package com.nungil.Service;

import com.nungil.Document.VideoList;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VideoListService {
    private final VideoRepository videoRepository;

    public VideoListService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    // 전체 데이터 조회
    public List<VideoList> getAllVideos() {
        return videoRepository.findAll();
    }

    // 특정 영화 조회
    public Optional<VideoList> getVideoByTitle(String title) {
        return videoRepository.findByTitle(title);
    }

    public List<VideoListDTO> getVideosWithPagination(int page, int size) {
        Page<VideoList> videoPage = videoRepository.findAll(PageRequest.of(page, size));

        return videoPage.getContent().stream()
                .map(video -> new VideoListDTO(
                        video.getId(),
                        video.getTitle(),
                        (video.getPosters() != null && !video.getPosters().isEmpty()) ? video.getPosters().get(0) : null
                ))
                .collect(Collectors.toList());
    }
}

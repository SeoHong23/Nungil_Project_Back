package com.nungil.Controller;

import com.nungil.Document.VideoList;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Service.VideoListService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
public class VideoListController {
    private final VideoListService videoService;

    public VideoListController(VideoListService videoService) {
        this.videoService = videoService;
    }

    // 모든 영화 조회
    @GetMapping
    public List<VideoList> getAllVideos() {
        return videoService.getAllVideos();
    }

    // 특정 영화 조회 (제목 기준)
    @GetMapping("/{title}")
    public Optional<VideoList> getVideoByTitle(@PathVariable String title) {
        return videoService.getVideoByTitle(title);
    }
    @GetMapping("/paged")
    public List<VideoListDTO> getPagedVideos(@RequestParam int page, @RequestParam int size) {
        return videoService.getVideosWithPagination(page, size);
    }

}

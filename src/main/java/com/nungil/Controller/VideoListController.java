package com.nungil.Controller;

import com.nungil.Document.VideoList;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Service.VideoListService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
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
    public List<VideoListDTO> getPagedVideos(@RequestParam int page,
                                             @RequestParam int size,
                                             @RequestParam(required = false) String orderBy,
                                             @RequestParam Map<String, String> filters) { // ✅ 필터를 동적으로 받음


        // ✅ 필터 데이터 가공 (필수값 제외)
        Map<String, Set<String>> processedFilters = filters.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("page") && !entry.getKey().equals("size") && !entry.getKey().equals("orderBy"))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new HashSet<>(Arrays.asList(entry.getValue().split(","))) // 쉼표 기준 분할
                ));
        log.info(processedFilters);
        // 디폴트로 최신순
        Sort type = Sort.by(Sort.Direction.DESC, "releaseDate");
        if (orderBy.equals("DateASC")) {
            type = Sort.by(Sort.Direction.ASC, "releaseDate");
        }

        // ✅ 필터가 없는 경우 일반 조회, 있는 경우 필터링

        return processedFilters.isEmpty()
                ? videoService.getVideosWithPagination(page, size, type)
                : videoService.getVideosWithFilter(page, size, processedFilters, type);
    }
    @GetMapping("/random")
    public List<VideoListDTO> getVideoRandom(@RequestParam int size) {
        return videoService.getVideoRandom(size);
    }
}

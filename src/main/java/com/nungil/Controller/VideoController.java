package com.nungil.Controller;

import com.nungil.Dto.VideoDTO;
import com.nungil.Service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class VideoController {
    private final VideoService videoService;

    /**
     * JSON 데이터를 외부 API 검색해서 가져와 저장하는 엔드포인트
     *
     * @param search 검색할 키워드 / 없을시 매일 자정에 시행하는 것과 같은 동작 시행
     * @return 처리 결과 String
     */
    @PostMapping
    public String fetchAndSaveData(@RequestParam(required = false) String search) {
        try {
            if (search != null && !search.isEmpty()) {
                videoService.searchDataFromApi(search);
            } else {
                videoService.saveDataFromApi();
            }
            return "API 데이터가 성공적으로 저장되었습니다.";
        } catch (Exception e) {
            return "API 데이터 저장 중 오류 발생: " + e.getMessage();
        }
    }

    /**
     * ID 값을 받아와 해당 id의 MongoDB 데이터를 조회하는 엔드포인트
     *
     * @param id 찾으려는 video 객체의 ID 값
     * @return 찾아낸 객체 데이터 DTO
     */
    @GetMapping
    public VideoDTO fetchVideoData(@RequestParam String id) {
        return videoService.readVideo(id);
    }

}

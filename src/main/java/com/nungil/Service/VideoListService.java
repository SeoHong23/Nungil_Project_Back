package com.nungil.Service;

import com.nungil.Document.VideoList;
import com.nungil.Dto.KobisResponseDTO;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Dto.VideoRankResponseDTO;
import com.nungil.Repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoListService {

    private final RestTemplate restTemplate;
    private static final String API_KEY = "fbee189dee8db43996847b8ff1a4bcbe"; // KOBIS API Key
    private static final String DAILY_URL = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json";



    private final VideoRepository videoRepository;

    public VideoListService(VideoRepository videoRepository, RestTemplate restTemplate) {
        this.videoRepository = videoRepository;
        this.restTemplate = restTemplate;
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

    public List<VideoRankResponseDTO> getDailyBoxOffice(String targetDate) {
        String url = UriComponentsBuilder.fromHttpUrl(DAILY_URL)
                .queryParam("key", API_KEY)
                .queryParam("targetDt", targetDate)
                .toUriString();

        KobisResponseDTO response = restTemplate.getForObject(url, KobisResponseDTO.class);

        if (response == null || response.getDailyBoxOfficeList() == null) {
            throw new IllegalStateException("Kobis API 응답이 null이거나 데이터가 없습니다. (targetDate: " + targetDate + ")\n" + url + "\n" + response);
        }

        List<KobisResponseDTO.MovieInfo> dailyBoxOfficeList = response.getDailyBoxOfficeList();
        // 2️⃣ MongoDB에서 commCode(영화 코드) 기준으로 매칭되는 데이터 가져오기
        List<String> movieCodes = dailyBoxOfficeList.stream()
                .map(KobisResponseDTO.MovieInfo::getMovieCd)
                .collect(Collectors.toList());

        List<VideoList> videoList = videoRepository.findByCommCodeIn(movieCodes);

        // 3️⃣ MongoDB 데이터 매핑 (commCode -> VideoList 매핑)
        Map<String, VideoList> videoMap = videoList.stream()
                .collect(Collectors.toMap(VideoList::getCommCode, v -> v));

        // 4️⃣ KOBIS 데이터와 MongoDB 데이터 매칭
        return dailyBoxOfficeList.stream()
                .map(movie -> {
                    VideoList matchedVideo = videoMap.get(movie.getMovieCd());

                    return new VideoRankResponseDTO(
                            matchedVideo != null ? matchedVideo.getId() : null,
                            matchedVideo != null ? matchedVideo.getTitle() : movie.getMovieNm(),
                            matchedVideo != null && matchedVideo.getPosters() != null && !matchedVideo.getPosters().isEmpty()
                                    ? matchedVideo.getPosters().get(0)
                                    : "",
                            movie.getRank(),
                            movie.getRankInten()
                    );
                })
                .collect(Collectors.toList());
    }


    public List<VideoRankResponseDTO> getWeeklyBoxOffice(String targetDate) {
        String url = UriComponentsBuilder.fromHttpUrl(WEEKLY_URL)
                .queryParam("key", API_KEY)
                .queryParam("targetDt", targetDate)
                .queryParam("weekGb", "0")
                .toUriString();

        KobisResponseDTO response = restTemplate.getForObject(url, KobisResponseDTO.class);

        if (response == null || response.getWeeklyBoxOfficeList() == null) {
            throw new IllegalStateException("Kobis API 응답이 null이거나 데이터가 없습니다. (targetDate: " + targetDate + ")\n" + url + "\n" + response);
        }

        List<KobisResponseDTO.MovieInfo> weeklyBoxOfficeList = response.getWeeklyBoxOfficeList();
        // 2️⃣ MongoDB에서 commCode(영화 코드) 기준으로 매칭되는 데이터 가져오기
        List<String> movieCodes = weeklyBoxOfficeList.stream()
                .map(KobisResponseDTO.MovieInfo::getMovieCd)
                .collect(Collectors.toList());

        List<VideoList> videoList = videoRepository.findByCommCodeIn(movieCodes);

        // 3️⃣ MongoDB 데이터 매핑 (commCode -> VideoList 매핑)
        Map<String, VideoList> videoMap = videoList.stream()
                .collect(Collectors.toMap(VideoList::getCommCode, v -> v));

        // 4️⃣ KOBIS 데이터와 MongoDB 데이터 매칭
        return weeklyBoxOfficeList.stream()
                .map(movie -> {
                    VideoList matchedVideo = videoMap.get(movie.getMovieCd());

                    return new VideoRankResponseDTO(
                            matchedVideo != null ? matchedVideo.getId() : null,
                            matchedVideo != null ? matchedVideo.getTitle() : movie.getMovieNm(),
                            matchedVideo != null && matchedVideo.getPosters() != null && !matchedVideo.getPosters().isEmpty()
                                    ? matchedVideo.getPosters().get(0)
                                    : "",
                            movie.getRank(),
                            movie.getRankInten()
                    );
                })
                .collect(Collectors.toList());
    }
}

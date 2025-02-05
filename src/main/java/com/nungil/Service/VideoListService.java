package com.nungil.Service;

import com.nungil.Document.VideoList;
import com.nungil.Dto.KobisResponseDTO;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Dto.VideoRankResponseDTO;
import com.nungil.Repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoListService {

    private final RestTemplate restTemplate;
    private static final String API_KEY = "fbee189dee8db43996847b8ff1a4bcbe"; // KOBIS API Key
    private static final String DAILY_URL = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json";



    private final VideoRepository videoRepository;
    private final MongoTemplate mongoTemplate;

    public VideoListService(VideoRepository videoRepository, RestTemplate restTemplate, MongoTemplate mongoTemplate) {
        this.videoRepository = videoRepository;
        this.restTemplate = restTemplate;
        this.mongoTemplate = mongoTemplate;
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

    public List<VideoListDTO> getVideosWithFilter(int page, int size, Map<String, Set<String>> filters) {

        Query query = new Query();

        Map<String, String> keyMapping = Map.of(
                "장르", "genre",
                "국가", "nation",
                "연도", "releaseDate"
        );


        filters.forEach((key, values) -> {
            String mappedKey = keyMapping.getOrDefault(key, key);

            if (!values.isEmpty()) {
                // ✅ "연도" 필터인 경우, releaseDate의 앞 4자리(YYYY)만 비교하도록 설정
                if (mappedKey.equals("releaseDate")) {
                    List<Criteria> yearCriterias = values.stream()
                            .map(year -> Criteria.where("releaseDate").regex("^" + year)) // ✅ 정규식: "YYYY"로 시작하는 값 필터링
                            .collect(Collectors.toList());

                    query.addCriteria(new Criteria().orOperator(yearCriterias.toArray(new Criteria[0])));
                } else {
                    query.addCriteria(Criteria.where(mappedKey).in(values));
                }
            }
        });

        query.with(PageRequest.of(page, size));



        List<VideoList> videoList = mongoTemplate.find(query, VideoList.class);

        return videoList.stream()
                .map(video -> new VideoListDTO(
                        video.getId(),
                        video.getTitle(),
                        (video.getPosters() != null && !video.getPosters().isEmpty()) ? video.getPosters().get(0) : null
                ))
                .collect(Collectors.toList());
    }

    public List<VideoRankResponseDTO> getBoxOffice(String targetDate, String type) {
        String url = "";
        if(type.equals("daily")){
            url = UriComponentsBuilder.fromHttpUrl(DAILY_URL)
                    .queryParam("key", API_KEY)
                    .queryParam("targetDt", targetDate)
                    .toUriString();
        }else if(type.equals("weekly")){
            url = UriComponentsBuilder.fromHttpUrl(WEEKLY_URL)
                    .queryParam("key", API_KEY)
                    .queryParam("targetDt", targetDate)
                    .queryParam("weekGb", "0")
                    .toUriString();
        }



        KobisResponseDTO response = restTemplate.getForObject(url, KobisResponseDTO.class);

        if (response == null || (response.getDailyBoxOfficeList() == null && response.getWeeklyBoxOfficeList() == null)) {
            throw new IllegalStateException("Kobis API 응답이 null이거나 데이터가 없습니다. (targetDate: " + targetDate + ")\n" + url + "\n" + response);
        }

        List<KobisResponseDTO.MovieInfo> dailyBoxOfficeList = null;

        if(type.equals("daily")){
            dailyBoxOfficeList = response.getDailyBoxOfficeList();
        }else if(type.equals("weekly")){
            dailyBoxOfficeList = response.getWeeklyBoxOfficeList();
        }

        // 1️⃣ 1차적으로 영화 코드(commCode) 매핑
        List<String> movieCodes = dailyBoxOfficeList.stream()
                .map(KobisResponseDTO.MovieInfo::getMovieCd)
                .collect(Collectors.toList());

        List<VideoList> videoListByCommCode = videoRepository.findByCommCodeIn(movieCodes);

        // 2️⃣ MongoDB 데이터 맵핑 (commCode -> VideoList 매핑)
        Map<String, VideoList> videoMapByCommCode = videoListByCommCode.stream()
                .collect(Collectors.toMap(VideoList::getCommCode, v -> v));

        // 3️⃣ 1차적으로 매칭되지 않은 영화들에 대해 title 기준으로 추가 검색
        List<String> unmatchedTitles = dailyBoxOfficeList.stream()
                .filter(movie -> !videoMapByCommCode.containsKey(movie.getMovieCd())) // commCode 기준으로 매칭되지 않은 영화들 필터링
                .map(KobisResponseDTO.MovieInfo::getMovieNm)
                .collect(Collectors.toList());

        List<VideoList> videoListByTitle = videoRepository.findByTitleIn(unmatchedTitles);

        // 4️⃣ MongoDB 데이터 맵핑 (title -> VideoList 매핑)
        Map<String, VideoList> videoMapByTitle = videoListByTitle.stream()
                .collect(Collectors.toMap(VideoList::getTitle, v -> v));
        log.info(dailyBoxOfficeList.toString());
        // 5️⃣ KOBIS 데이터와 MongoDB 데이터 매칭
        return dailyBoxOfficeList.stream()
                .map(movie -> {
                    // 1️⃣ 우선 commCode로 매칭
                    VideoList matchedVideo = videoMapByCommCode.get(movie.getMovieCd());

                    // 2️⃣ commCode로 찾지 못한 경우, title 기반 매칭
                    if (matchedVideo == null) {
                        matchedVideo = videoMapByTitle.get(movie.getMovieNm());
                    }

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

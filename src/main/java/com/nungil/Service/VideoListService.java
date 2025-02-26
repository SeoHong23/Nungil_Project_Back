package com.nungil.Service;

import com.nungil.Document.RankingCacheDocument;
import com.nungil.Document.VideoList;
import com.nungil.Dto.KobisResponseDTO;
import com.nungil.Dto.VideoListDTO;
import com.nungil.Dto.VideoRankResponseDTO;
import com.nungil.Repository.VideoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoListService {

    private final RestTemplate restTemplate;
    private static final String API_KEY = "fbee189dee8db43996847b8ff1a4bcbe"; // KOBIS API Key
    private static final String DAILY_URL = "https://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json";
    private static final String WEEKLY_URL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchWeeklyBoxOfficeList.json";


    private final CacheService cacheService;
    private final VideoRepository videoRepository;
    private final MongoTemplate mongoTemplate;

    public VideoListService(VideoRepository videoRepository, RestTemplate restTemplate, CacheService cacheService, MongoTemplate mongoTemplate) {
        this.videoRepository = videoRepository;
        this.restTemplate = restTemplate;
        this.cacheService = cacheService;
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
    public List<VideoListDTO> getVideoRandom(int size) {
        return videoRepository.findRandom(size).stream().map(video -> new VideoListDTO(
                        video.getId(),
                        video.getTitle(),
                        (video.getPosters() != null && !video.getPosters().isEmpty()) ? video.getPosters().get(0) : null
                ))
                .collect(Collectors.toList());
    }


    public List<VideoListDTO> getVideosWithPagination(int page, int size, Sort orderBy, boolean isNotOpen) {


        Query query = new Query();

        // 🔥 오늘 날짜를 "20250205" 형식으로 변환
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String today = sdf.format(new Date());

        query.with(orderBy.and(Sort.by("_id")));
        if(!isNotOpen) {
            query.addCriteria(Criteria.where("releaseDate").lte(today));
        }else{
            query.addCriteria(Criteria.where("releaseDate").gt(today));
        }

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

    public List<VideoListDTO> getVideosWithFilter(int page, int size, Map<String, Set<String>> filters, Sort orderBy, boolean isNotOpen) {
        Query query = new Query();

        Map<String, String> keyMapping = Map.of(
                "장르", "genre",
                "국가", "nation",
                "연도", "releaseDate",
                "연령등급", "rating"
        );

        // ✅ 오늘 날짜를 "yyyyMMdd" 형식으로 변환 (예: 20250205)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String today = sdf.format(new Date());

        // ✅ 장르, 국가, 연도, 연령등급 필터 적용
        filters.forEach((key, values) -> {
            String mappedKey = keyMapping.getOrDefault(key, key);

            if (!values.isEmpty()) {
                if (mappedKey.equals("releaseDate")) {
                    List<Criteria> yearCriterias = values.stream()
                            .map(year -> Criteria.where("releaseDate").regex("^" + year)) // ✅ 연도 필터링
                            .toList();

                    query.addCriteria(new Criteria().orOperator(yearCriterias.toArray(new Criteria[0])));
                } else if(mappedKey.equals("OTT")){

                }
                else{
                    query.addCriteria(Criteria.where(mappedKey).in(values));
                }
            }
        });

        // ✅ OTT 필터 적용 (불필요한 "OTT" 필터 제거, "ottInfo.ottPlatform"만 필터링)
        if (filters.containsKey("OTT")) {
            Set<String> ottPlatforms = filters.get("OTT");
            if (ottPlatforms != null && !ottPlatforms.isEmpty()) {
                query.addCriteria(Criteria.where("ottInfo.platform").in(ottPlatforms));
            }
        }

        // ✅ 개봉일 기준 필터 추가
        if(!isNotOpen) {
            query.addCriteria(Criteria.where("releaseDate").lte(today));
        }else{
            query.addCriteria(Criteria.where("releaseDate").gt(today));
        }

        // ✅ 정렬 기준 추가 (id 포함)
        query.with(orderBy.and(Sort.by("_id")));

        // ✅ 페이지네이션 적용
        query.with(PageRequest.of(page, size));


        // ✅ MongoDB에서 데이터 조회
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

        // 캐싱
        Optional<RankingCacheDocument> cachedData = cacheService.getFromCache(targetDate,type);
        if (cachedData.isPresent()) {
            System.out.println("✅ 캐시에서 데이터 반환");
            return (List<VideoRankResponseDTO>) cachedData.get().getData();
        }

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
                .map(movie -> movie.getMovieNm().trim()) // 영화 제목에 trim() 적용
                .collect(Collectors.toList());

        List<VideoList> videoListByTitle = videoRepository.findByTitleIn(unmatchedTitles);

        // title 기준으로 최신 releaseDate를 가진 영화만 선택
        Map<String, VideoList> latestMoviesByTitle = videoListByTitle.stream()
                .collect(Collectors.toMap(
                        VideoList::getTitle,  // key: 영화 제목
                        Function.identity(),  // value: VideoList 객체
                        (existing, replacement) ->
                                existing.getReleaseDate().compareTo(replacement.getReleaseDate()) >= 0
                                        ? existing : replacement // 최신 releaseDate 선택
                ));

// 최종적으로 최신 개봉일을 가진 영화 목록
        List<VideoList> latestVideoList = new ArrayList<>(latestMoviesByTitle.values());


        // 4️⃣ MongoDB 데이터 맵핑 (title -> VideoList 매핑)
        Map<String, VideoList> videoMapByTitle = latestVideoList.stream()
                .collect(Collectors.toMap(VideoList::getTitle, v -> v));
        // 5️⃣ KOBIS 데이터와 MongoDB 데이터 매칭



        List<VideoRankResponseDTO> result = dailyBoxOfficeList.stream()
                .map(movie -> {
                    // 1️⃣ 우선 commCode로 매칭
                    VideoList matchedVideo = videoMapByCommCode.get(movie.getMovieCd());

                    // 2️⃣ commCode로 찾지 못한 경우, title 기반 매칭
                    if (matchedVideo == null) {
                        matchedVideo = videoMapByTitle.get(movie.getMovieNm().trim());
                    }

                    return new VideoRankResponseDTO(
                            matchedVideo != null ? matchedVideo.getId() : null,
                            matchedVideo != null ? matchedVideo.getTitle() : movie.getMovieNm(),
                            matchedVideo != null && matchedVideo.getPosters() != null && !matchedVideo.getPosters().isEmpty()
                                    ? matchedVideo.getPosters().get(0)
                                    : "",
                            movie.getRank(),
                            movie.getRankInten(),
                            movie.getRankOldAndNew()
                    );
                })
                .collect(Collectors.toList());

        cacheService.saveToCache(result, targetDate, type);

        return result;
    }

    public List<VideoListDTO> getVideosWithQuery(int page, int size, String keywords, String searchType) {
        Query query = new Query();

        // ✅ 키워드 분리
        String[] keywordArray = keywords.split(" ");
        List<Criteria> keywordCriteria = new ArrayList<>();

        // ✅ 검색 대상 필드 지정
        if (searchType != null && !searchType.isEmpty()) {
            for (String keyword : keywordArray) {
                keywordCriteria.add(Criteria.where(searchType).regex(keyword, "i")); // 🔥 대소문자 무시 검색
            }
            query.addCriteria(new Criteria().orOperator(keywordCriteria.toArray(new Criteria[0])));
        }


            // ✅ 정렬 기준 추가 (id 포함)
        query.with(Sort.by(Sort.Order.desc("releaseDate")).and(Sort.by("_id")));

        // ✅ 페이지네이션 적용
        query.with(PageRequest.of(page, size));


        // ✅ MongoDB에서 데이터 조회
        List<VideoList> videoList = mongoTemplate.find(query, VideoList.class);


        return videoList.stream()
                .map(video -> new VideoListDTO(
                        video.getId(),
                        video.getTitle(),
                        (video.getPosters() != null && !video.getPosters().isEmpty()) ? video.getPosters().get(0) : null
                ))
                .collect(Collectors.toList());
    }
}

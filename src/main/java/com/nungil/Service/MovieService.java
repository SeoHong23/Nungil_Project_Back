package com.nungil.Service;

import com.mongodb.client.result.UpdateResult;
import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import com.nungil.Repository.Interfaces.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MongoTemplate mongoTemplate;
    private final MovieRepository movieRepository;
    private final KinoService kinoService;

    @Autowired
    public MovieService(MongoTemplate mongoTemplate, MovieRepository movieRepository,KinoService kinoService) {
        this.mongoTemplate = mongoTemplate;
        this.movieRepository = movieRepository;
        this.kinoService = kinoService;
    }


    public MovieDocument findMovieByTitle(String title) {
        Query query = new Query(Criteria.where("title")
                .regex("^\\s*" + title.trim().replaceAll("[^a-zA-Z0-9가-힣]", "") + "\\s*$", "i")); // 대소문자 무시
        return mongoTemplate.findOne(query, MovieDocument.class);
    }
    private String normalize(String title) {
        return title.trim().replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase();
    }

    /**
     * MongoDB에 저장된 영화의 OTT 정보를 업데이트하는 메서드
     */
    public Map<String, Object> updateOttInfo(String title) {
        Map<String, Object> result = new HashMap<>();

        // 1️⃣ MongoDB에서 영화 조회
        Query query = new Query(Criteria.where("title").regex("^\\s*" + title.trim().replaceAll("[^a-zA-Z0-9가-힣]", "") + "\\s*$", "i"));
        MovieDocument existingMovie = mongoTemplate.findOne(query, MovieDocument.class);

        if (existingMovie == null) {
            result.put("message", "해당 영화가 MongoDB에 존재하지 않습니다.");
            return result;
        }

        // 2️⃣ 키노라이츠 크롤링
        List<MovieDTO> kinoMovies = kinoService.fetchMoviesByTitle(title);
        if (kinoMovies.isEmpty()) {
            result.put("message", "키노라이츠에서 영화 정보를 찾을 수 없습니다.");
            return result;
        }

        // 3️⃣ OTT 정보 업데이트
        MovieDTO matchedMovie = kinoMovies.stream()
                .filter(kinoMovie -> normalize(kinoMovie.getTitle()).equals(normalize(existingMovie.getTitle())))
                .findFirst()
                .orElse(null);

        if (matchedMovie != null) {
            boolean updateSuccess = updateOTTLinksByTitle(existingMovie.getTitle(), matchedMovie.getOttInfo());
            result.put("ottInfo", matchedMovie.getOttInfo());
            result.put("message", updateSuccess ? "OTT 정보가 성공적으로 업데이트되었습니다." : "OTT 정보 업데이트에 실패했습니다.");
        } else {
            result.put("message", "키노라이츠와 일치하는 영화 정보를 찾을 수 없습니다.");
            result.put("kinoTitles", kinoMovies.stream().map(MovieDTO::getTitle).collect(Collectors.toList()));
        }

        return result;
    }


    /**
     * MongoDB에서 영화 제목이 존재할 경우 OTT 정보를 업데이트
     */
    public boolean updateOTTLinksByTitle(String title, List<MovieDTO.OTTInfo> ottInfoList) {
        // 제목 정규화 (공백 및 특수문자 제거)
        String normalizedTitle = title.trim().replaceAll("[^a-zA-Z0-9가-힣]", "").toLowerCase();

        // MongoDB에서 대소문자 구분 없이 공백 제거 후 제목 검색
        Query query = new Query(Criteria.where("title")
                .regex("^\\s*" + normalizedTitle + "\\s*$", "i")); // 대소문자 무시 (i)

        Update update = new Update().set("ottInfo", convertToMovieDocumentOttInfo(ottInfoList));
        UpdateResult result = mongoTemplate.updateFirst(query, update, MovieDocument.class);

        if (result.getMatchedCount() > 0) {
            System.out.println("✅ OTT 정보가 업데이트됨: " + title);
            return true;
        } else {
            System.out.println("🚨 업데이트 실패 (제목 불일치 가능성 있음): " + title);
            return false;
        }
    }


    /**
     * DTO → MovieDocument의 OTTInfo 변환 메서드
     */
    private List<MovieDocument.OTTInfo> convertToMovieDocumentOttInfo(List<MovieDTO.OTTInfo> dtoList) {
        return dtoList.stream()
                .map(dto -> new MovieDocument.OTTInfo(dto.getOttPlatform(), dto.getAvailable(), dto.getLink()))
                .collect(Collectors.toList());
    }

    /**
     * OTT 정보를 크롤링하여 업데이트 후 반환
     */
    private MovieDocument updateOttInfoForMovie(MovieDocument movie) {
        // 키노라이츠 크롤링
        List<MovieDTO> kinoMovies = kinoService.fetchMoviesByTitle(movie.getTitle());

        if (!kinoMovies.isEmpty()) {
            MovieDTO kinoMovie = kinoMovies.get(0); // 첫 번째 크롤링 결과 사용

            // OTT 정보 업데이트
            updateOTTLinksByTitle(movie.getTitle(), kinoMovie.getOttInfo());

            // 업데이트된 MongoDB 데이터 반환
            return movieRepository.findByTitle(movie.getTitle()).orElse(movie);
        }

        return movie; // OTT 정보 없이 반환
    }







    //    /**
//     * MongoDB 또는 KOBIS에서 영화 정보를 검색하고, OTT 정보를 업데이트하는 메서드
//     */
//    public MovieDocument searchOrFetchMovie(String title) {
//        // 1️⃣ MongoDB에서 검색
//        MovieDocument existingMovie = movieRepository.findByTitle(title).orElse(null);
//        if (existingMovie != null) {
//            System.out.println("✅ MongoDB에서 데이터를 가져왔습니다: " + title);
//            return updateOttInfoForMovie(existingMovie); // OTT 정보 업데이트 후 반환
//        }
//
//        // 2️⃣ MongoDB에 없으면 KOBIS에서 가져오기
//        System.out.println("🚨 MongoDB에 데이터가 없습니다. KOBIS API를 호출합니다: " + title);
//        MovieDocument movieFromKobis = fetchMovieFromKobis(title);
//
//        if (movieFromKobis != null) {
//            // KOBIS 데이터를 MongoDB에 저장
//            movieRepository.save(movieFromKobis);
//            System.out.println("✅ KOBIS에서 데이터를 가져와 MongoDB에 저장했습니다: " + title);
//            return updateOttInfoForMovie(movieFromKobis); // OTT 정보 업데이트 후 반환
//        }
//
//        // 3️⃣ KOBIS에서도 데이터를 가져오지 못한 경우
//        System.out.println("🚨 KOBIS에서도 데이터를 찾을 수 없습니다: " + title);
//        return null;
//    }



//    /**
//     * KOBIS API를 통해 영화 데이터를 가져오는 메서드
//     */
//    private MovieDocument fetchMovieFromKobis(String title) {
//        // KOBIS API 호출
//        JsonNode movieNode = kobisService.getMovieByTitle(title);
//
//        if (movieNode != null && movieNode.size() > 0) {
//            JsonNode movieData = movieNode.get(0); // 첫 번째 영화 데이터 사용
//
//            // KOBIS 데이터를 MovieDocument로 변환
//            MovieDocument movie = new MovieDocument();
//            movie.setTitle(movieData.path("movieNm").asText());
//            movie.setReleaseDate(movieData.path("openDt").asText());
//            movie.setGenre(List.of(movieData.path("genreAlt").asText().split(",")));
//            movie.setType("");
//            movie.setOttInfo(List.of()); // OTT 정보는 크롤링에서 추가
//            return movie;
//        }
//
//        return null;
//    }


}

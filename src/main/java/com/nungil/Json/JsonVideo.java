package com.nungil.Json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import com.nungil.Document.PlotDocument;
import com.nungil.Document.StaffDocument;
import com.nungil.Document.VideoDocument;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonVideo {
    @JsonProperty("title")
    private String title;

    @JsonProperty("titleEng")
    private String titleEng;

    @JsonProperty("titleOrg")
    private String titleOrg;

    @JsonProperty("titleEtc")
    private String titleEtc;

    @JsonProperty("prodYear")
    private String prodYear;

    @JsonProperty("nation")
    private String nation;

    @JsonProperty("company")
    private String company;

    @JsonProperty("plots")
    private PlotWrapper plots;

    @JsonProperty("runtime")
    private String runtime;

    @JsonProperty("rating")
    private String rating;

    @JsonProperty("genre")
    private String genre;

    @JsonProperty("type")
    private String type;

    @JsonProperty("use")
    private String use;

    @JsonProperty("episodes")
    private String episodes;

    @JsonProperty("repRlsDate")
    private String repRlsDate;

    @JsonProperty("keywords")
    private String keywords;

    @JsonProperty("posters")
    private String posters;

    @JsonProperty("stlls")
    private String stlls;

    @JsonProperty("staffs")
    private StaffWrapper staffs;

    @JsonProperty("Awards1")
    private String awards1;

    @JsonProperty("Awards2")
    private String awards2;

    @JsonProperty("CommCodes")
    private CodeWrapper commCodes;

    public VideoDocument toVideoDocument() {
        this.transferData();
        return VideoDocument.builder()
                .commCode(commCodes != null && !commCodes.getCodeList().isEmpty() ? commCodes.getCodeList().get(0).getCodeNo() : null)
                .title(title)
                .titleEng(titleEng)
                .titleOrg(titleOrg)
                .titleEtc(titleEtc)
                .prodYear(prodYear)
                .nation(nation)
                .company(company != null ? Arrays.stream(company.split(","))
                        .filter(c -> !c.isEmpty()) // 빈 값은 필터링
                        .toList() : new ArrayList<>())
                .plots(plots.getPlotList() != null ? plots.getPlotList().stream().map(Plot::toDocument).toList() : new ArrayList<>()) // Null 체크 후 변환
                .runtime(runtime)
                .rating(rating)
                .genre(genre != null ? Arrays.stream(genre.split(","))
                        .filter(g -> !g.isEmpty()) // 빈 값은 필터링
                        .toList() : new ArrayList<>())
                .type(type)
                .use(use)
                .releaseDate(repRlsDate)
                .posters(posters != null ? Arrays.stream(posters.split("\\|"))
                        .filter(p -> !p.isEmpty()) // 빈 값은 필터링
                        .toList() : new ArrayList<>())
                .stlls(stlls != null ? Arrays.stream(stlls.split("\\|"))
                        .filter(s -> !s.isEmpty()) // 빈 값은 필터링
                        .toList() : new ArrayList<>())
                .staffs(staffs != null ? staffs.getStaffList().stream().map(Staff::toDocument).toList() : new ArrayList<>()) // Null 체크 후 변환
                .awards1(awards1)
                .awards2(awards2)
                .keywords(keywords != null ? Arrays.stream(keywords.split(","))
                        .filter(k -> !k.isEmpty())
                        .toList() : new ArrayList<>())
                .build();
    }

    private void transferData() {
        title = title.trim();
        if(titleEng != null) {
            titleEng = titleEng.replaceAll("\\s?\\([^)]*\\)", "");
            titleEng = titleEng.trim();
        }
        if (type.equals("애니메이션")) genre = type + "," + genre;
        if (genre.contains("코메디")) genre = genre.replaceAll("코메디", "코미디");
        if (rating != null && !rating.isEmpty()) {
            rating = rating.replaceAll("세", "세 ").replaceAll("관", " 관");
            rating = rating.replaceAll("세  관", "세 이상 관");
        }
        if (nation.contains(",")) nation = nation.replaceAll(",", ", ");

        nation = nation.replaceAll("대한민국", "한국");
    }


}

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
class PlotWrapper {
    @JsonProperty("plot")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Plot> plotList = new ArrayList<>();
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Plot {
    @JsonProperty("plotLang")
    private String plotLang;

    @JsonProperty("plotText")
    private String plotText;

    PlotDocument toDocument() {
        return PlotDocument.builder()
                .plotText(transferPlot(plotText))
                .plotLang(plotLang)
                .build();
    }

    private String transferPlot(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "";
        }

        // 괄호와 괄호 안의 내용을 먼저 제거
        inputText = removeParentheses(inputText).replaceAll("\\.{3,}","….");
        // 작은 따옴표와 특수 따옴표를 제거 (안의 내용은 남김)
        inputText = removeQuotationMarks(inputText);

        final List<String> noSpaceBefore = Arrays.asList(".", ",", "!", "?", "…", ")", "”");
        final List<String> noSpaceAfter = Arrays.asList("(", "“");

        // HanLP로 토큰화 (각 Term에는 단어 정보가 들어 있음)
        List<Term> terms = HanLP.segment(inputText);
        StringBuilder sb = getStringBuilder(terms, noSpaceBefore, noSpaceAfter);
        // 긴 공백 자르기
        String result = sb.toString().trim().replaceAll("\\s+", " ");
        result = result.replaceAll("([.!?])([^\\s])", "$1 $2"); // 문장 부호 뒤에 공백 추가

        return result;
    }

    private String removeParentheses(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "";
        }
        // 둥근 괄호 안의 내용 제거
        inputText = inputText.replaceAll("\\([^)]*\\)", "");
        // 대괄호 안의 내용 제거
        inputText = inputText.replaceAll("\\[[^\\]]*\\]", "");
        // 중괄호 안의 내용 제거
        inputText = inputText.replaceAll("\\{[^}]*\\}", "");
        return inputText;
    }

    // 작은 따옴표, 특수 따옴표 제거 (안의 내용은 그대로 두기)
    private String removeQuotationMarks(String inputText) {
        if (inputText == null || inputText.isEmpty()) {
            return "";
        }

        inputText = inputText.replaceAll("(?<=\\S)['‘’]+(?=\\S)", ""); // 단어와 단어 사이의 작은 따옴표와 특수 따옴표 제거
        inputText = inputText.replaceAll("['‘’]+", ""); // 작은 따옴표와 특수 따옴표를 모두 제거

        return inputText;
    }

    @NotNull
    private static StringBuilder getStringBuilder(List<Term> terms, List<String> noSpaceBefore, List<String> noSpaceAfter) {
        StringBuilder sb = new StringBuilder();
        String prev = "";

        for (Term term : terms) {
            String token = term.word;

            if (!sb.isEmpty() && noSpaceBefore.contains(token)) {
                sb.append(token);
            } else {
                if (!sb.isEmpty() && !noSpaceAfter.contains(prev)) {
                    sb.append(" ");
                }
                sb.append(token);
            }
            prev = token;
        }
        return sb;
    }
}

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
class StaffWrapper {
    @JsonProperty("staff")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Staff> staffList = new ArrayList<>();
}

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
class Staff {
    @JsonProperty("staffNm")
    private String staffNm;

    @JsonProperty("staffEnNm")
    private String staffEnNm;

    @JsonProperty("staffRoleGroup")
    private String staffRoleGroup;

    @JsonProperty("staffRole")
    private String staffRole;

    @JsonProperty("staffEtc")
    private String staffEtc;

    @JsonProperty("staffId")
    private String staffId;

    StaffDocument toDocument() {
        return StaffDocument.builder()
                .staffNm(staffNm)
                .staffId(staffId)
                .staffRoleGroup(staffRoleGroup)
                .staffRole(staffRole)
                .build();
    }
}

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
class CodeWrapper {
    @JsonProperty("CommCode")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Code> codeList = new ArrayList<>();
}

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
class Code {
    @JsonProperty("CodeNo")
    private String codeNo;


}

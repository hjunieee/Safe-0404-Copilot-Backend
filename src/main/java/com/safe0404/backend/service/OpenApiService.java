package com.safe0404.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safe0404.backend.entity.Country;
import com.safe0404.backend.entity.SafetyNotice;
import com.safe0404.backend.repository.CountryRepository;
import com.safe0404.backend.repository.SafetyNoticeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

// 외교부 공공 API 연동 서비스
@Service
@RequiredArgsConstructor
public class OpenApiService {

    private final CountryRepository countryRepository;
    private final SafetyNoticeRepository safetyNoticeRepository;

    @Value("${openapi.service.key}")
    private String serviceKey;

    @Value("${openapi.url.warning}")
    private String warningUrl;

    @Value("${openapi.url.notice}")
    private String noticeUrl;

    // 초기 Mock 데이터 적재
    @PostConstruct
    public void initMockData() {
        if (countryRepository.count() == 0) {
            List<Country> mockCountries = new ArrayList<>();
            mockCountries.add(new Country("JP", "일본", 1, "여행유의 (1단계)", "110", "119", "+81-3-3452-7611", "오키나와 인근 지진 발생에 따른 쓰나미 여파 주의. 해안가 접근 자제 요망."));
            mockCountries.add(new Country("FR", "프랑스", 2, "여행자제 (2단계)", "17", "15", "+33-1-4753-0101", "파리 시내 대규모 시위 예정. 인파 밀집 지역 방문 자제 요망."));
            mockCountries.add(new Country("PH", "필리핀", 3, "철수권고 (3단계)", "911", "911", "+63-2-8856-7188", "민다나오 지역 치안 악화 지속. 해당 지역 체류자는 즉시 철수 요망."));
            mockCountries.add(new Country("UA", "우크라이나", 4, "여행금지 (4단계)", "102", "103", "+48-22-742-0300", "우크라이나 전역 전쟁 위기 심화. 즉시 안전한 인근 국가로 대피 요망."));

            countryRepository.saveAll(mockCountries);

            List<SafetyNotice> mockNotices = new ArrayList<>();
            // 일본 가이드
            mockNotices.add(new SafetyNotice(null, "JP", "여권을 분실했을 때 대처 요령", "1. 가까운 경찰서에서 분실 신고서 작성\n2. 주일본 대사관 방문하여 긴급 단수여권 발급 신청\n3. 여권용 사진 2매 및 수수료 필요.", "2026-07-17"));
            mockNotices.add(new SafetyNotice(null, "JP", "지진 발생 시 행동 지침", "1. 테이블 밑으로 들어가 머리 보호\n2. 흔들림 멈춘 후 침착하게 외부 공터로 대피\n3. 승강기 사용 절대 금지. 계단 이용.", "2026-07-17"));
            
            // 프랑스 가이드
            mockNotices.add(new SafetyNotice(null, "FR", "소매치기 피해 예방 및 송금제도", "1. 현지 경찰서 신고 및 분실증명서 획득\n2. 외교부 신속해외송금 제도 활용 (국내 송금 시 대사관에서 현지화로 수령)", "2026-07-17"));
            
            // 우크라이나 가이드
            mockNotices.add(new SafetyNotice(null, "UA", "여행금지 특별 지침", "1. 현지 대한민국 대사관 당직 연결 요망\n2. 무단 입국 시 여권법 위반으로 형사 처벌 대상이 될 수 있습니다.", "2026-07-17"));

            safetyNoticeRepository.saveAll(mockNotices);
        }
    }

    // 30분 주기 데이터 실시간 갱신 배치
    @Scheduled(cron = "0 0/30 * * * ?")
    public void syncOpenApiData() {
        if ("your_actual_service_key_here".equals(serviceKey) || serviceKey.isEmpty()) {
            return;
        }
        
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 여행경보 API 연동
        try {
            String url = UriComponentsBuilder.fromUriString(warningUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", "100")
                    .queryParam("pageNo", "1")
                    .queryParam("returnType", "JSON")
                    .build(true) // 인코딩 자동 처리 방지
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data");

            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    String code = node.path("country_iso_alp2").asText("");
                    if (code.isEmpty()) continue;

                    String name = node.path("country_nm").asText("");
                    Integer level = node.path("alarm_lvl").asInt(0);
                    String warningText = node.path("alarm_lvl_nm").asText("");

                    Country country = countryRepository.findById(code.toUpperCase())
                            .orElse(new Country());

                    country.setCode(code.toUpperCase());
                    country.setName(name);
                    country.setWarningLevel(level);
                    country.setWarningText(warningText);

                    // 비상번호 디폴트 매핑
                    if (country.getPolice() == null) country.setPolice("112");
                    if (country.getAmbulance() == null) country.setAmbulance("119");
                    if (country.getEmbassy() == null) country.setEmbassy("미등록");

                    countryRepository.save(country);
                }
            }
        } catch (Exception e) {
            System.err.println("[오류] 여행경보 API 연동 실패: " + e.getMessage());
            e.printStackTrace();
        }

        // 2. 안전공지 API 연동
        try {
            String url = UriComponentsBuilder.fromUriString(noticeUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", "30")
                    .queryParam("pageNo", "1")
                    .queryParam("returnType", "JSON")
                    .build(true)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode dataNode = root.path("data");

            if (dataNode.isArray()) {
                for (JsonNode node : dataNode) {
                    String code = node.path("country_iso_alp2").asText("");
                    if (code.isEmpty()) continue;

                    String title = node.path("title").asText("");
                    String content = node.path("txt_origin").asText("");
                    String date = node.path("wrt_dt").asText("");

                    List<SafetyNotice> existing = safetyNoticeRepository.findByCountryCode(code.toUpperCase());
                    boolean duplicate = existing.stream().anyMatch(n -> n.getTitle().equals(title));

                    if (!duplicate) {
                        SafetyNotice notice = new SafetyNotice();
                        notice.setCountryCode(code.toUpperCase());
                        notice.setTitle(title);
                        notice.setContent(content);
                        notice.setWrittenDate(date);
                        safetyNoticeRepository.save(notice);

                        countryRepository.findById(code.toUpperCase()).ifPresent(c -> {
                            c.setRecentNotice(title);
                            countryRepository.save(c);
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[오류] 안전공지 API 연동 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

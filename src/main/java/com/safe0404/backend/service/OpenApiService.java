package com.safe0404.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safe0404.backend.entity.Country;
import com.safe0404.backend.entity.CountryEnvironment;
import com.safe0404.backend.entity.SafetyNotice;
import com.safe0404.backend.repository.CountryEnvironmentRepository;
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
    private final CountryEnvironmentRepository countryEnvironmentRepository;
    private final VectorStoreService vectorStoreService;

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

            // 종합환경 Mock 데이터 추가
            List<CountryEnvironment> mockEnvs = new ArrayList<>();
            mockEnvs.add(new CountryEnvironment("JP", "일본", 99.8, "최고 수준의 안전한 수질 및 보건망. 응급 대응 체계 우수.", 104.5, 2.6, 9.2, "치안 양호. 소매치기 수준의 경범죄 외 전반적 치안 위협 극히 낮음. 단, 지진 자연재해 다발.", "주일본 대사관: +81-3-3452-7611"));
            mockEnvs.add(new CountryEnvironment("FR", "프랑스", 98.5, "의료 시스템 우수. 주요 거점 병원에 양질의 장비 확보.", 112.3, 7.2, 6.8, "파리 대도시 기차역 인근 강절도 및 소매치기 다발. 야간 으슥한 골목 출입 자제 요망.", "주프랑스 대사관: +33-1-4753-0101"));
            mockEnvs.add(new CountryEnvironment("PH", "필리핀", 87.2, "주요 거점 사립 병원 양호하나 외곽 시골 지역은 수질 전염병 및 뎅기열 위약.", 125.1, 4.8, 245.0, "남부 민다나오 특별 치안 악화 구역. 이슬람 반군 납치 테러 위협으로 야간 보행 통제.", "주필리핀 대사관: +63-2-8856-7188"));
            mockEnvs.add(new CountryEnvironment("UA", "우크라이나", 82.0, "전쟁 사태 장기화로 국지적 병원 폭격 파괴 심각. 기본 의약품 및 수술 기자재 부족.", 140.0, 15.0, 65.0, "전역 여행금지 발령. 계엄령 하 치안 통제 불안정. 군사 충돌 및 미사일 공습 진행.", "주폴란드 대외 대피지원팀: +48-22-742-0300"));

            countryEnvironmentRepository.saveAll(mockEnvs);
            
            // 데이터 적재가 완료되었으므로 벡터 DB 재빌딩 트리거
            vectorStoreService.rebuildVectorStore();
        }
    }

    // 30분 주기 데이터 실시간 갱신 배치
    @Scheduled(cron = "0 0/30 * * * ?")
    public void syncOpenApiData() {
        System.out.println("[시스템] 데이터 동기화 배치를 시작합니다. 현재 로드된 API Key: [" + serviceKey + "]");
        
        if ("your_actual_service_key_here".equals(serviceKey) || serviceKey.isEmpty()) {
            System.err.println("[경고] API 인증키가 감지되지 않았거나 기본 템플릿 값입니다. .env 설정을 확인해 주세요. 동기화를 실행하지 않고 조기 종료합니다.");
            return;
        }
        
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        boolean updated = false;

        // 1. 여행경보 V3 API 연동
        try {
            String url = UriComponentsBuilder.fromUriString(warningUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", "100")
                    .queryParam("pageNo", "1")
                    .queryParam("returnType", "JSON")
                    .build(true) // 인코딩 자동 처리 방지
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("[디버그] 여행경보 API 응답 원본: " + (response.getBody() != null ? response.getBody().substring(0, Math.min(1000, response.getBody().length())) : "null"));
            JsonNode root = objectMapper.readTree(response.getBody());
            // response -> body -> items -> item 경로로 수정
            JsonNode itemNode = root.path("response").path("body").path("items").path("item");

            System.out.println("[디버그] 여행경보 itemNode IsArray: " + itemNode.isArray() + ", Size: " + itemNode.size());

            if (itemNode.isArray() && itemNode.size() > 0) {
                updated = true;
                for (JsonNode node : itemNode) {
                    String isoCode = node.path("iso_code").asText("");
                    if (isoCode.isEmpty()) continue;

                    String code = node.path("country_iso_alp2").asText("");
                    if (code.isEmpty()) {
                        code = convertIso3ToIso2(isoCode);
                    }
                    if (code.isEmpty()) continue;

                    String name = node.path("country_name").asText("");
                    
                    // 각 항목의 노트를 대조하여 경보레벨 1~4 판별
                    int level = 0;
                    String warningText = "경보 없음";
                    if (!node.path("ban_note").asText("").isEmpty() || !node.path("ban_yna").asText("").isEmpty()) {
                        level = 4;
                        warningText = "여행금지 (4단계)";
                    } else if (!node.path("limita").asText("").isEmpty() || !node.path("limita_note").asText("").isEmpty()) {
                        level = 3;
                        warningText = "철수권고 (3단계)";
                    } else if (!node.path("control").asText("").isEmpty() || !node.path("control_note").asText("").isEmpty()) {
                        level = 2;
                        warningText = "여행자제 (2단계)";
                    } else if (!node.path("attention").asText("").isEmpty() || !node.path("attention_note").asText("").isEmpty()) {
                        level = 1;
                        warningText = "여행유의 (1단계)";
                    }

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

        // 2. 안전공지 V6 API 연동
        try {
            String url = UriComponentsBuilder.fromUriString(noticeUrl)
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("numOfRows", "30")
                    .queryParam("pageNo", "1")
                    .queryParam("returnType", "JSON")
                    .build(true)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("[디버그] 안전공지 API 응답 원본: " + (response.getBody() != null ? response.getBody().substring(0, Math.min(1000, response.getBody().length())) : "null"));
            JsonNode root = objectMapper.readTree(response.getBody());
            // response -> body -> items -> item 경로로 수정
            JsonNode dataNode = root.path("response").path("body").path("items").path("item");
            System.out.println("[디버그] 안전공지 dataNode IsArray: " + dataNode.isArray() + ", Size: " + dataNode.size());

            if (dataNode.isArray() && dataNode.size() > 0) {
                updated = true;
                for (JsonNode node : dataNode) {
                    String code = node.path("country_iso_alp2").asText("");
                    if (code.isEmpty()) continue;

                    String title = node.path("title").asText("");
                    // V2의 txt_origin 대신 V6의 txt_origin_cn 사용
                    String content = node.path("txt_origin_cn").asText("");
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

        // 실제 수집 데이터 적재 성공 시 Vector DB 리인덱싱 수행
        if (updated) {
            System.out.println("[시스템] 데이터 적재 완료에 따른 실시간 벡터 DB 재색인을 개시합니다.");
            vectorStoreService.rebuildVectorStore();
        }
    }

    // 3자리 ISO 코드를 2자리 국가코드로 변환하는 헬퍼
    private String convertIso3ToIso2(String iso3) {
        if (iso3 == null || iso3.isEmpty()) return "";
        switch (iso3.toUpperCase()) {
            case "JPN": return "JP";
            case "FRA": return "FR";
            case "PHL": return "PH";
            case "UKR": return "UA";
            case "USA": return "US";
            case "CHN": return "CN";
            case "GBR": return "GB";
            case "DEU": return "DE";
            case "ITA": return "IT";
            case "VNM": return "VN";
            case "THA": return "TH";
            default:
                return iso3.substring(0, Math.min(2, iso3.length())).toUpperCase();
        }
    }
}

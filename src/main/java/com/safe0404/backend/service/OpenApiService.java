package com.safe0404.backend.service;

import com.safe0404.backend.entity.Country;
import com.safe0404.backend.entity.SafetyNotice;
import com.safe0404.backend.repository.CountryRepository;
import com.safe0404.backend.repository.SafetyNoticeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
            // API Key 미주입 시 패스
            return;
        }
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            // TODO: 실제 외교부 OpenAPI 호출 및 DB 파싱 적재 파이프라인 연동 예정 (스프린트 2 진입부)
        } catch (Exception e) {
            // 통신 예외 발생 시 로깅
        }
    }
}

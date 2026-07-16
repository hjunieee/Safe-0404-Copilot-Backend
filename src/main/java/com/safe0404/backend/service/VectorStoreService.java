package com.safe0404.backend.service;

import com.safe0404.backend.entity.Country;
import com.safe0404.backend.entity.CountryEnvironment;
import com.safe0404.backend.entity.SafetyNotice;
import com.safe0404.backend.repository.CountryEnvironmentRepository;
import com.safe0404.backend.repository.CountryRepository;
import com.safe0404.backend.repository.SafetyNoticeRepository;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// 벡터 DB 구축 및 텍스트 유사도 검색 서비스
@Service
@RequiredArgsConstructor
public class VectorStoreService {

    private final CountryRepository countryRepository;
    private final SafetyNoticeRepository safetyNoticeRepository;
    private final CountryEnvironmentRepository countryEnvironmentRepository;

    private EmbeddingStore<TextSegment> embeddingStore;
    private EmbeddingModel embeddingModel;

    @PostConstruct
    public void init() {
        // 자바 네이티브 결정론적 해시 임베딩 모델 장착 (네이티브 라이브러리 충돌 및 로딩 지연 원천 제거)
        embeddingModel = new LocalWordEmbeddingModel();
        embeddingStore = new InMemoryEmbeddingStore<>();
        rebuildVectorStore();
    }

    // DB 데이터 기반으로 벡터 스토어 재빌드
    public synchronized void rebuildVectorStore() {
        embeddingStore = new InMemoryEmbeddingStore<>();
        
        List<Country> countries = countryRepository.findAll();
        for (Country c : countries) {
            // 1. 국가 기본 안전 정보 청크 생성
            String countryText = String.format(
                "국가: %s (%s). 여행경보단계: %s. 긴급번호: 경찰 %s, 구급차 %s. 재외공관 연락망: %s. 최근소식: %s",
                c.getName(), c.getCode(), c.getWarningText(), c.getPolice(), c.getAmbulance(), c.getEmbassy(), c.getRecentNotice()
            );
            addTextToStore(countryText, c.getCode(), "country_info");

            // 2. 해당 국가의 안전 공지사항 리스트 청크 생성
            List<SafetyNotice> notices = safetyNoticeRepository.findByCountryCode(c.getCode());
            for (SafetyNotice sn : notices) {
                String noticeText = String.format(
                    "국가코드: %s. 제목: %s. 가이드라인 본문: %s. 작성일: %s",
                    sn.getCountryCode(), sn.getTitle(), sn.getContent(), sn.getWrittenDate()
                );
                addTextToStore(noticeText, c.getCode(), "safety_notice");
            }

            // 3. 해당 국가의 치안/의료 환경 지표 청크 생성
            countryEnvironmentRepository.findById(c.getCode()).ifPresent(env -> {
                String envText = String.format(
                    "국가: %s (%s). 음용수 깨끗함 비율: %.1f%%. 의료인프라 수준: %s. 소비자 물가지수(CPI): %.1f. 실업률: %.1f%%. 결핵 발병률: %.1f. 치안 수준 설명: %s. 재외공관 연락망: %s",
                    env.getName(), env.getCode(), env.getWaterRatio() != null ? env.getWaterRatio() : 0.0,
                    env.getMedicalInfrastructure() != null ? env.getMedicalInfrastructure() : "보통",
                    env.getCpi() != null ? env.getCpi() : 100.0,
                    env.getUnemploymentRate() != null ? env.getUnemploymentRate() : 0.0,
                    env.getTuberculosisRate() != null ? env.getTuberculosisRate() : 0.0,
                    env.getSecurityIndex() != null ? env.getSecurityIndex() : "보통",
                    env.getDiplomaticHotlines() != null ? env.getDiplomaticHotlines() : "미등록"
                );
                addTextToStore(envText, c.getCode(), "environment");
            });
        }
    }

    private void addTextToStore(String text, String countryCode, String type) {
        TextSegment segment = TextSegment.from(text);
        segment.metadata().add("countryCode", countryCode);
        segment.metadata().add("type", type);

        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }

    // 유사 문서 탐색 (유사도 검색)
    public List<String> searchRelevantContexts(String query, int maxResults) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(queryEmbedding, maxResults);
        
        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.toList());
    }

    // 무겁고 운영체제 종속적인 ONNX 바이너리 의존성을 제거한 자바 순수 가상 임베딩 구현체
    private static class LocalWordEmbeddingModel implements EmbeddingModel {
        @Override
        public dev.langchain4j.model.output.Response<Embedding> embed(String text) {
            float[] vector = new float[384];
            // 형태소 분석기를 사용하지 않고, 공백 기준 토큰화 및 해싱을 통한 유사도 매핑
            String[] tokens = text.toLowerCase().split("\\s+");
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                int hash = Math.abs(token.hashCode() % 384);
                vector[hash] += 1.0f;
            }
            
            // L2 정규화 (코사인 유사도 내적 연산 호환을 위해 크기를 1.0으로 고정)
            float sum = 0.0f;
            for (float v : vector) sum += v * v;
            float norm = (float) Math.sqrt(sum);
            if (norm > 0) {
                for (int i = 0; i < 384; i++) vector[i] /= norm;
            }
            return dev.langchain4j.model.output.Response.from(Embedding.from(vector));
        }

        @Override
        public dev.langchain4j.model.output.Response<Embedding> embed(TextSegment textSegment) {
            return embed(textSegment.text());
        }

        @Override
        public dev.langchain4j.model.output.Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
            List<Embedding> list = new ArrayList<>();
            for (TextSegment seg : textSegments) {
                list.add(embed(seg.text()).content());
            }
            return dev.langchain4j.model.output.Response.from(list);
        }
    }
}

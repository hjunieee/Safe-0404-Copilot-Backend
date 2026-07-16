package com.safe0404.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 국가 종합환경 정보 엔티티
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountryEnvironment {
    @Id
    private String code; // 국가코드
    private String name; // 국가명
    
    // 의료환경 데이터
    private Double waterRatio; // 음용수 사용 비율
    private String medicalInfrastructure; // 의료인프라요약
    
    // 종합인프라 및 사회지표
    private Double cpi; // 소비자물가지수
    private Double unemploymentRate; // 실업률
    private Double tuberculosisRate; // 결핵발병률
    
    // 치안 및 공관 정보
    @Column(columnDefinition = "TEXT")
    private String securityIndex; // 치안환경텍스트
    @Column(columnDefinition = "TEXT")
    private String diplomaticHotlines; // 공관연락망정보
}

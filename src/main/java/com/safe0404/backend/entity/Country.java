package com.safe0404.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 국가 안전 데이터 엔티티
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Country {
    @Id
    private String code; // 국가코드
    private String name; // 국가명
    private Integer warningLevel; // 경보레벨
    private String warningText; // 경보단계설명
    private String police; // 경찰전화번호
    private String ambulance; // 구급차전화번호
    private String embassy; // 대사관연락처
    private String recentNotice; // 최신공지요약
}

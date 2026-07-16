package com.safe0404.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 안전 공지사항 엔티티
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SafetyNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 식별자
    private String countryCode; // 국가코드
    private String title; // 공지제목
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String content; // 공지내용
    private String writtenDate; // 작성일자
}

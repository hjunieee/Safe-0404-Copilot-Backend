package com.safe0404.backend.repository;

import com.safe0404.backend.entity.SafetyNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

// 안전 공지사항 레포지토리
@Repository
public interface SafetyNoticeRepository extends JpaRepository<SafetyNotice, Long> {
    // 국가코드 기반 조회
    List<SafetyNotice> findByCountryCode(String countryCode);
}

package com.safe0404.backend.controller;

import com.safe0404.backend.entity.Country;
import com.safe0404.backend.entity.SafetyNotice;
import com.safe0404.backend.repository.CountryRepository;
import com.safe0404.backend.repository.SafetyNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 프론트엔드 연동 API 컨트롤러
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // React 개발 포트 허용
@RequiredArgsConstructor
public class ApiController {

    private final CountryRepository countryRepository;
    private final SafetyNoticeRepository safetyNoticeRepository;

    // 전체 국가 목록 조회
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryRepository.findAll());
    }

    // 특정 국가 상세 조회
    @GetMapping("/countries/{code}")
    public ResponseEntity<Country> getCountryByCode(@PathVariable String code) {
        return countryRepository.findById(code.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 국가별 안전 공지사항 조회
    @GetMapping("/countries/{code}/notices")
    public ResponseEntity<List<SafetyNotice>> getNoticesByCountry(@PathVariable String code) {
        return ResponseEntity.ok(safetyNoticeRepository.findByCountryCode(code.toUpperCase()));
    }
}

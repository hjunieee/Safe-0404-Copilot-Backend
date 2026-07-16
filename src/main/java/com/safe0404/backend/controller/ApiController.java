package com.safe0404.backend.controller;

import com.safe0404.backend.entity.Country;
import com.safe0404.backend.entity.SafetyNotice;
import com.safe0404.backend.repository.CountryRepository;
import com.safe0404.backend.repository.SafetyNoticeRepository;
import com.safe0404.backend.service.OpenApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 프론트엔드 연동 API 컨트롤러
@Tag(name = "Safe 0404 Copilot API", description = "외교부 공공데이터 기반 실시간 국가 경보 및 재외국민 안전 공지 조회 API")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // React 개발 포트 허용
@RequiredArgsConstructor
public class ApiController {

    private final CountryRepository countryRepository;
    private final SafetyNoticeRepository safetyNoticeRepository;
    private final OpenApiService openApiService;

    // 수동 데이터 동기화 수행
    @Operation(summary = "실시간 공공데이터 즉시 동기화", description = "외교부 공공 API(여행경보 및 안전공지)로부터 최신 정보를 수집하여 데이터베이스에 즉시 갱신합니다.")
    @GetMapping("/sync")
    public ResponseEntity<String> forceSyncData() {
        openApiService.syncOpenApiData();
        return ResponseEntity.ok("외교부 공공 API 데이터 동기화 완료!");
    }

    // 전체 국가 목록 조회
    @Operation(summary = "전체 국가 안전 정보 조회", description = "데이터베이스에 적재된 전 세계 국가의 안전 등급 및 경보 요약 정보를 한꺼번에 조회합니다.")
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(countryRepository.findAll());
    }

    // 특정 국가 상세 조회
    @Operation(summary = "특정 국가 상세 안전 정보 조회", description = "국가코드(2자리, 예: JP)를 기반으로 해당 국가의 구체적인 여행 경보 상태 및 비상 연락처 정보를 조회합니다.")
    @GetMapping("/countries/{code}")
    public ResponseEntity<Country> getCountryByCode(@PathVariable String code) {
        return countryRepository.findById(code.toUpperCase())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 국가별 안전 공지사항 조회
    @Operation(summary = "국가별 실시간 안전 공지사항 조회", description = "국가코드(2자리, 예: FR)를 기반으로 해당 국가에 발령된 최신 사건사고 및 재해/재난 대처 매뉴얼 공지 목록을 조회합니다.")
    @GetMapping("/countries/{code}/notices")
    public ResponseEntity<List<SafetyNotice>> getNoticesByCountry(@PathVariable String code) {
        return ResponseEntity.ok(safetyNoticeRepository.findByCountryCode(code.toUpperCase()));
    }
}

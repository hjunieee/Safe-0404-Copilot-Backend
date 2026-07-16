package com.safe0404.backend.controller;

import com.safe0404.backend.dto.MedicalCardRequestDto;
import com.safe0404.backend.dto.MedicalCardResponseDto;
import com.safe0404.backend.service.MedicalCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 다국어 긴급 의료진술 카드 API 컨트롤러
@Tag(name = "0404 Medical Card API", description = "터치형 자가증상 입력을 다국어 의학 진술문으로 번역/정제하는 긴급 의료 조력 API")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // React 개발 포트 허용
@RequiredArgsConstructor
public class MedicalCardController {

    private final MedicalCardService medicalCardService;

    // 다국어 의학 카드 생성 수행
    @Operation(summary = "다국어 의학 긴급 카드 생성", description = "환자의 단순 증상 명칭과 기저질환을 취합하여, 현지 의료진 제시를 위한 표준 의학 진술문 및 긴급 행동 요령을 영문 및 현지어(일본어/프랑스어 등)로 실시간 생성합니다.")
    @PostMapping("/medical-card")
    public ResponseEntity<MedicalCardResponseDto> createMedicalCard(@RequestBody MedicalCardRequestDto request) {
        if (request.getPatientName() == null || request.getPatientName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getSymptoms() == null || request.getSymptoms().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getTargetLanguage() == null || request.getTargetLanguage().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        MedicalCardResponseDto response = medicalCardService.generateMedicalCard(request);
        return ResponseEntity.ok(response);
    }
}

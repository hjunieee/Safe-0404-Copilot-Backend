package com.safe0404.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// 다국어 의학 카드 생성 요청 DTO
@Schema(description = "다국어 의학 카드 생성 요청 바디 데이터")
@Getter
@Setter
public class MedicalCardRequestDto {

    @Schema(description = "환자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String patientName;

    @Schema(description = "한국어 증상 리스트", example = "[\"가슴 통증\", \"호흡 곤란\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> symptoms;

    @Schema(description = "기저질환 리스트", example = "[\"천식\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> chronicDiseases;

    @Schema(description = "번역할 목표 언어코드 (JA: 일본어, FR: 프랑스어, EN: 영어)", example = "JA", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetLanguage;
}

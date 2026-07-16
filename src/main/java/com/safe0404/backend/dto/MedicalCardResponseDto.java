package com.safe0404.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 다국어 의학 카드 결과 응답 DTO
@Schema(description = "생성 완료된 다국어 의학 긴급 카드 데이터")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCardResponseDto {

    @Schema(description = "환자 이름", example = "홍길동")
    private String patientName;

    @Schema(description = "정제된 한국어 의학 진술문 요약", example = "기저 천식 환자로서 호흡 곤란을 동반한 급성 흉부 통증 발현.")
    private String koreanStatement;

    @Schema(description = "글로벌 표준 영문 의학 진술문", example = "Patient has a history of asthma and is presenting with acute chest pain accompanied by dyspnea.")
    private String englishStatement;

    @Schema(description = "지정 언어로 번역 및 현지화된 의학 진술문 (현지 의료진 제시용)", example = "喘息の既往歴があり、呼吸困難を伴う急性胸部痛を呈しています。")
    private String translatedStatement;

    @Schema(description = "AI 권장 비상 행동 수칙 및 주의사항", example = "1. 안정을 취하고 필요시 환자의 휴대용 흡입기(벤토린 등) 사용을 조력하십시오.\n2. 즉시 현지 응급의료지원(119)에 연결하십시오.")
    private String precautions;
}

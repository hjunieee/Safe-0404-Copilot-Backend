package com.safe0404.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

// AI 챗봇 질문 요청 DTO
@Schema(description = "0404 AI 챗봇 질문 요청 데이터")
@Getter
@Setter
public class ChatRequestDto {
    
    @Schema(description = "사용자가 챗봇에게 보낸 질문 메시지", example = "일본 여권 분실했을 때 대처 방법 알려줘", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

    @Schema(description = "선택적인 2자리 국가코드 (대시보드 검색 연동용)", example = "JP", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String countryCode;
}

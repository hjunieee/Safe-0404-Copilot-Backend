package com.safe0404.backend.controller;

import com.safe0404.backend.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 0404 AI 챗봇 연동 컨트롤러
@Tag(name = "0404 AI Chat API", description = "재외국민 안전 공지 지식 기반 0404 AI 1:1 RAG 챗봇 대화 API")
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173") // React 개발 포트 허용
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    // AI RAG 대화 수행
    @Operation(summary = "0404 AI RAG 챗봇 질의", description = "외교부 지식 베이스 및 실시간 사건사고 데이터를 연동하여, 사용자의 비상 질문에 신뢰성 높은 마크다운 형식의 조력 답변을 생성합니다.")
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> askCopilot(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String countryCode = request.get("countryCode");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "message 필드는 비워둘 수 없습니다."));
        }
        
        String answer = ragService.askAi(message, countryCode);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}

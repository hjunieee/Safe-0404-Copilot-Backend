package com.safe0404.backend.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

// 0404 AI RAG 검색 증강 생성 답변 서비스
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStoreService vectorStoreService;

    @Value("${litellm.api.url}")
    private String apiUrl;

    @Value("${litellm.api.key}")
    private String apiKey;

    @Value("${litellm.model.name}")
    private String modelName;

    // AI 질문 및 답변 연동
    public String askAi(String message, String countryCode) {
        // 1. LiteLLM 인증키가 없거나 임시값일 때 Mock 챗봇 폴백 가동
        if ("none".equals(apiKey) || apiKey.isEmpty() || "your_actual_litellm_key_here".equals(apiKey)) {
            return generateMockAnswer(message, countryCode);
        }

        try {
            // 2. 유사도 기반 Vector DB 지식 베이스 검색
            List<String> contexts = vectorStoreService.searchRelevantContexts(message, 3);
            StringBuilder contextBuilder = new StringBuilder();
            for (String ctx : contexts) {
                contextBuilder.append("- ").append(ctx).append("\n");
            }

            // 3. OpenAI 규격 호환 클라이언트로 LiteLLM 서버 바인딩
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl(apiUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(60))
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            // 4. 시스템 프롬프트 및 사용자 지식 주입 프롬프트 설계
            String systemPrompt = "당신은 대한민국 외교부의 가상 AI 비서 '0404 AI'입니다. " +
                    "오직 제공되는 [신뢰성 있는 외교부 안전 지식]만을 참고하여 정직하고 정갈하게 한국어로 답변해야 합니다. " +
                    "제시된 지식에 기반하지 않은 무분별한 억측이나 거짓말은 절대 해서는 안 됩니다. " +
                    "답변은 마크다운(Markdown) 포맷으로 보기 편하게 구조화하여 반환해 주세요.";

            String userPrompt = String.format(
                    "[신뢰성 있는 외교부 안전 지식]\n%s\n\n[사용자 질문]\n%s",
                    contextBuilder.toString(), message
            );

            // 5. 생성 모델 호출
            return model.generate(userPrompt);

        } catch (Exception e) {
            System.err.println("[오류] LiteLLM API 통신 실패: " + e.getMessage());
            return "죄송합니다. 현재 외부 AI API 연동 상태가 불안정하여 답변을 드릴 수 없습니다. 임시 가이드라인을 확인해 주시거나 현지 공관으로 직접 연락하시길 권장합니다. (사유: " + e.getMessage() + ")";
        }
    }

    // API Key 부재 시 동작하는 로컬 가상 응답기
    private String generateMockAnswer(String message, String countryCode) {
        String code = (countryCode != null) ? countryCode.toUpperCase() : "";
        if (message.contains("여권") || message.contains("분실")) {
            return "### 0404 AI 긴급 안내 (가상 챗봇)\n\n" +
                    "해외에서 여권을 분실하셨을 때의 행동 요령입니다:\n\n" +
                    "1. **현지 경찰서 방문**: 즉시 인근 경찰서로 이동하여 **여권 분실 신고서(Report)**를 발급받으세요.\n" +
                    "2. **재외공관(대사관/총영사관) 방문**: 여권 사진 2매와 분실 신고서를 가지고 대한민국 대사관에 방문하여 **단수 여권** 또는 긴급 여권 발급을 신청하세요.\n" +
                    "3. **비상 연락망**: 현지 공관 번호로 연락하시면 24시간 당직 긴급 조력을 받으실 수 있습니다.";
        } else if (message.contains("지진") || message.contains("재난")) {
            return "### 0404 AI 긴급 안내 (가상 챗봇)\n\n" +
                    "지진 및 자연 재난 상황에서의 긴급 대처 행동 수칙입니다:\n\n" +
                    "1. **실내에 있을 때**: 튼튼한 탁자나 테이블 밑으로 들어가 머리를 감싸고 흔들림이 멈출 때까지 대기하세요.\n" +
                    "2. **실외로 이동 시**: 낙하물(간판, 유리창)로부터 머리를 보호하며 넓은 공터로 신속히 이동하세요.\n" +
                    "3. **승강기 탑승 절대 금지**: 피난 시 승강기는 정전 위험이 크므로 계단을 이용하셔야 합니다.";
        }
        
        return "### 0404 AI (가상 챗봇)\n\n" +
                "질문 주신 내용에 대해 외교부 공공 데이터베이스를 탐색했습니다.\n" +
                "현재 API Key가 연동되지 않아 가상 모드로 동작 중입니다. 구체적인 RAG 답변을 확인하시려면 `.env` 파일에 유효한 `LITELLM_API_KEY`를 주입해 주세요.";
    }
}

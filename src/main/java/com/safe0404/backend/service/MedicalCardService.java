package com.safe0404.backend.service;

import com.safe0404.backend.dto.MedicalCardRequestDto;
import com.safe0404.backend.dto.MedicalCardResponseDto;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

// 다국어 의학 카드 생성 서비스
@Service
public class MedicalCardService {

    @Value("${litellm.api.url}")
    private String apiUrl;

    @Value("${litellm.api.key}")
    private String apiKey;

    @Value("${litellm.model.name}")
    private String modelName;

    // AI 다국어 의학 긴급 카드 생성
    public MedicalCardResponseDto generateMedicalCard(MedicalCardRequestDto request) {
        // 1. API Key 검증 및 Mock 폴백 동작
        if ("none".equals(apiKey) || apiKey.isEmpty() || "your_actual_litellm_key_here".equals(apiKey)) {
            return generateMockResponse(request);
        }

        try {
            // 2. OpenAI 호환 클라이언트로 LiteLLM 연동
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .baseUrl(apiUrl)
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .timeout(Duration.ofSeconds(60))
                    .build();

            // 3. RAG/Medical Copilot 전용 프롬프트 빌드
            String symptomsStr = String.join(", ", request.getSymptoms());
            String diseasesStr = request.getChronicDiseases() != null && !request.getChronicDiseases().isEmpty() 
                    ? String.join(", ", request.getChronicDiseases()) 
                    : "없음";
            String targetLangText = convertLanguageCodeToName(request.getTargetLanguage());

            String systemPrompt = "당신은 응급 다국어 의학 카드를 생성해 주는 조력 AI 'Medical Copilot'입니다. " +
                    "환자가 입력한 단순 한국어 증상 조합과 기저질환을 바탕으로, " +
                    "현지 의료진이 즉각 참고할 수 있는 고품질의 표준 의학 진술문(Medical Statement)으로 격상 및 가공해 번역해야 합니다. " +
                    "답변은 반드시 사족이나 인사말을 모두 생략하고 아래 지정된 '구분자 포맷'만을 완벽하게 포함해 출력해야 합니다.";

            String userPrompt = String.format(
                    "[환자 의료 기본 정보]\n" +
                    "- 이름: %s\n" +
                    "- 호소 증상 목록: %s\n" +
                    "- 기저질환 목록: %s\n" +
                    "- 번역할 목표 현지어: %s (코드: %s)\n\n" +
                    "위의 환자 정보를 바탕으로 아래 명시된 템플릿 구분자를 포함하여 채워주세요. 구분자 외의 다른 설명은 절대 추가하지 마세요.\n\n" +
                    "===KOREAN===\n" +
                    "(한국어 요약 진술문)\n" +
                    "===ENGLISH===\n" +
                    "(격상된 영어 의학 진술문)\n" +
                    "===TRANSLATED===\n" +
                    "(지정한 목표 현지어로 번역된 의학 진술문)\n" +
                    "===PRECAUTIONS===\n" +
                    "(환자와 주변인이 긴급히 취해야 할 안전 주의 조치 1~2가지)",
                    request.getPatientName(), symptomsStr, diseasesStr, targetLangText, request.getTargetLanguage()
            );

            // 4. 모델 호출 및 결과 파싱
            String rawResponse = model.generate(userPrompt);
            return parseAiResponse(request.getPatientName(), rawResponse);

        } catch (Exception e) {
            System.err.println("[오류] 의학 카드 API 호출 실패: " + e.getMessage());
            // 통신 에러 발생 시에도 정상 작동하게끔 Mock 폴백 제공
            return generateMockResponse(request);
        }
    }

    // AI 응답 구분자 파싱 모듈
    private MedicalCardResponseDto parseAiResponse(String name, String rawResponse) {
        String korean = "기본 증상 요약 발현.";
        String english = "Patient is presenting symptoms.";
        String translated = "Patient is presenting symptoms.";
        String precautions = "1. 안정을 취하고 현지 의료진의 조력을 기다리십시오.";

        try {
            String[] parts = rawResponse.split("===");
            for (int i = 0; i < parts.length; i++) {
                String section = parts[i].trim();
                if (section.equals("KOREAN") && i + 1 < parts.length) {
                    korean = parts[i + 1].trim();
                } else if (section.equals("ENGLISH") && i + 1 < parts.length) {
                    english = parts[i + 1].trim();
                } else if (section.equals("TRANSLATED") && i + 1 < parts.length) {
                    translated = parts[i + 1].trim();
                } else if (section.equals("PRECAUTIONS") && i + 1 < parts.length) {
                    precautions = parts[i + 1].trim();
                }
            }
        } catch (Exception e) {
            System.err.println("[경고] AI 응답 분할 파싱 예외 발생. 전체 원본 대체 주입. 사유: " + e.getMessage());
            korean = rawResponse;
        }

        return MedicalCardResponseDto.builder()
                .patientName(name)
                .koreanStatement(korean)
                .englishStatement(english)
                .translatedStatement(translated)
                .precautions(precautions)
                .build();
    }

    // 언어코드 ➡️ 한국어 언어명 매핑 헬퍼
    private String convertLanguageCodeToName(String code) {
        if (code == null) return "영어";
        switch (code.toUpperCase()) {
            case "JA": return "일본어";
            case "FR": return "프랑스어";
            case "EN": return "영어";
            default: return "영어";
        }
    }

    // 로컬 가짜 의료 카드 생성기 (Fallback)
    private MedicalCardResponseDto generateMockResponse(MedicalCardRequestDto request) {
        String symptoms = String.join(", ", request.getSymptoms());
        String diseases = request.getChronicDiseases() != null && !request.getChronicDiseases().isEmpty()
                ? String.join(", ", request.getChronicDiseases())
                : "없음";

        String targetLang = request.getTargetLanguage().toUpperCase();

        // 1. 디폴트 한국어 
        String korean = String.format("기저질환 [%s]을(를) 보유한 환자로, 현재 [%s] 증상을 호소하고 있음. 신속한 긴급 조치 요망.", diseases, symptoms);
        
        // 2. 디폴트 영어
        String english = String.format("Patient has a history of [%s] and is complaining of [%s]. Immediate medical evaluation is recommended.", diseases, symptoms);

        // 3. 디폴트 타겟 번역문 (일본어 / 프랑스어 대응)
        String translated = english;
        String precautions = "1. 환자를 조용하고 평평한 곳에 눕히고 호흡을 편안하게 유지하십시오.\n2. 즉시 현지 응급 센터 또는 대한민국 공관에 조력을 요청하십시오.";

        if ("JA".equals(targetLang)) {
            translated = String.format("既往歴［%s］を有する患者で、現在［%s］の症状を訴えています。速やかな救급処置を求めます。", diseases, symptoms);
            precautions = "1. 患者を安静にさせ、呼吸を楽に保ちます。\n2. 必要に応じて、最寄りの救急（119）または日本国内の韓国大使館にご連絡ください。";
        } else if ("FR".equals(targetLang)) {
            translated = String.format("Patient avec des antécédents de [%s] présentant des symptômes de [%s]. Une évaluation médicale immédiate est requise.", diseases, symptoms);
            precautions = "1. Allongez le patient dans un endroit calme et assurez-vous qu'il respire confortablement.\n2. Appelez immédiatement les urgences médicales locales (15) ou l'ambassade de Corée.";
        }

        return MedicalCardResponseDto.builder()
                .patientName(request.getPatientName())
                .koreanStatement(korean)
                .englishStatement(english)
                .translatedStatement(translated)
                .precautions(precautions)
                .build();
    }
}

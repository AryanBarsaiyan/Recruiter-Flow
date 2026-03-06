package com.futurescope.platform.ai.screening.client;

import com.futurescope.platform.ai.screening.client.dto.ResumeScreeningRequest;
import com.futurescope.platform.ai.screening.client.dto.ResumeScreeningResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class MockAiResumeScreeningClient implements AiResumeScreeningClient {

    @Override
    public ResumeScreeningResult screen(ResumeScreeningRequest request) {
        ResumeScreeningResult result = new ResumeScreeningResult();
        // Very simple mock: always shortlist with score 80
        result.setMatchScore(BigDecimal.valueOf(80.0));
        result.setResult("shortlisted");
        result.setExplanationJson("{\"reason\":\"mock screening always shortlists with score 80\"}");
        return result;
    }
}


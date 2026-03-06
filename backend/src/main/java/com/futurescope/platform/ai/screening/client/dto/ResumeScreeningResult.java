package com.futurescope.platform.ai.screening.client.dto;

import java.math.BigDecimal;

public class ResumeScreeningResult {

    private BigDecimal matchScore;
    private String result;
    private String explanationJson;

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(BigDecimal matchScore) {
        this.matchScore = matchScore;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getExplanationJson() {
        return explanationJson;
    }

    public void setExplanationJson(String explanationJson) {
        this.explanationJson = explanationJson;
    }
}


package com.futurescope.platform.ai.screening.client;

import com.futurescope.platform.ai.screening.client.dto.ResumeScreeningRequest;
import com.futurescope.platform.ai.screening.client.dto.ResumeScreeningResult;

public interface AiResumeScreeningClient {

    ResumeScreeningResult screen(ResumeScreeningRequest request);

}


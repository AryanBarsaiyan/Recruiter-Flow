package com.futurescope.platform.job.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.job.service.PipelineQueryService;
import com.futurescope.platform.job.web.dto.PipelineResponse;
import com.futurescope.platform.job.web.dto.PipelineStageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class PipelineController {

    private final PipelineQueryService pipelineQueryService;

    public PipelineController(PipelineQueryService pipelineQueryService) {
        this.pipelineQueryService = pipelineQueryService;
    }

    @GetMapping("/companies/{companyId}/pipelines")
    public ResponseEntity<List<PipelineResponse>> listPipelines(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID companyId
    ) {
        List<PipelineResponse> list = pipelineQueryService.listByCompany(companyId, currentUser);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/pipelines/{pipelineId}/stages")
    public ResponseEntity<List<PipelineStageResponse>> getStages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID pipelineId
    ) {
        List<PipelineStageResponse> list = pipelineQueryService.getStages(pipelineId, currentUser);
        return ResponseEntity.ok(list);
    }
}

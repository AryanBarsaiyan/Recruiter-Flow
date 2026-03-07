package com.futurescope.platform.job.web;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.job.service.PipelineQueryService;
import com.futurescope.platform.job.service.PipelineService;
import com.futurescope.platform.job.web.dto.CreateStageRequest;
import com.futurescope.platform.job.web.dto.PipelineStageResponse;
import com.futurescope.platform.job.web.dto.ReorderStagesRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
public class PipelineController {

    private final PipelineQueryService pipelineQueryService;
    private final PipelineService pipelineService;

    public PipelineController(PipelineQueryService pipelineQueryService, PipelineService pipelineService) {
        this.pipelineQueryService = pipelineQueryService;
        this.pipelineService = pipelineService;
    }

    @GetMapping("/pipelines/{pipelineId}/stages")
    public ResponseEntity<List<PipelineStageResponse>> getStages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID pipelineId
    ) {
        List<PipelineStageResponse> list = pipelineQueryService.getStages(pipelineId, currentUser);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/pipelines/{pipelineId}/stages")
    public ResponseEntity<PipelineStageResponse> addStage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID pipelineId,
            @Valid @RequestBody CreateStageRequest request
    ) {
        PipelineStageResponse response = pipelineService.addStage(pipelineId, request, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/pipelines/{pipelineId}/stages/reorder")
    public ResponseEntity<List<PipelineStageResponse>> reorderStages(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID pipelineId,
            @Valid @RequestBody ReorderStagesRequest request
    ) {
        List<PipelineStageResponse> response = pipelineService.reorderStages(pipelineId, request, currentUser);
        return ResponseEntity.ok(response);
    }
}

package com.futurescope.platform.job.service;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.CompanyRepository;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.job.domain.Pipeline;
import com.futurescope.platform.job.domain.PipelineStage;
import com.futurescope.platform.job.repository.PipelineRepository;
import com.futurescope.platform.job.repository.PipelineStageRepository;
import com.futurescope.platform.job.web.dto.CreatePipelineRequest;
import com.futurescope.platform.job.web.dto.CreateStageRequest;
import com.futurescope.platform.job.web.dto.PipelineResponse;
import com.futurescope.platform.job.web.dto.PipelineStageResponse;
import com.futurescope.platform.job.web.dto.ReorderStagesRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PipelineService {

    private static final String TYPE_RESUME_SCREENING = "resume_screening";
    private static final String TYPE_AI_INTERVIEW = "ai_interview";
    private static final String TYPE_OFFER = "offer";

    private final CompanyRepository companyRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final RbacService rbacService;

    public PipelineService(
            CompanyRepository companyRepository,
            PipelineRepository pipelineRepository,
            PipelineStageRepository pipelineStageRepository,
            RbacService rbacService
    ) {
        this.companyRepository = companyRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.rbacService = rbacService;
    }

    @Transactional
    public PipelineResponse createPipeline(@Valid CreatePipelineRequest request, User currentUser) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, company.getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));

        if (request.isDefault()) {
            pipelineRepository.findByCompany(company).forEach(p -> {
                p.setDefault(false);
                pipelineRepository.save(p);
            });
        }

        Pipeline pipeline = new Pipeline();
        pipeline.setId(UUID.randomUUID());
        pipeline.setCompany(company);
        pipeline.setName(request.getName());
        pipeline.setDefault(request.isDefault());
        pipeline.setCreatedAt(OffsetDateTime.now());
        pipelineRepository.save(pipeline);

        List<PipelineStage> stages = List.of(
                createStage(pipeline, "Resume Screening", TYPE_RESUME_SCREENING, 0),
                createStage(pipeline, "AI Interview", TYPE_AI_INTERVIEW, 1),
                createStage(pipeline, "Offer", TYPE_OFFER, 2)
        );
        stages.forEach(pipelineStageRepository::save);

        PipelineResponse resp = new PipelineResponse();
        resp.setId(pipeline.getId());
        resp.setCompanyId(pipeline.getCompany().getId());
        resp.setName(pipeline.getName());
        resp.setDefault(pipeline.isDefault());
        return resp;
    }

    private PipelineStage createStage(Pipeline pipeline, String name, String type, int orderIndex) {
        PipelineStage stage = new PipelineStage();
        stage.setId(UUID.randomUUID());
        stage.setPipeline(pipeline);
        stage.setName(name);
        stage.setType(type);
        stage.setOrderIndex(orderIndex);
        return stage;
    }

    @Transactional
    public PipelineStageResponse addStage(UUID pipelineId, CreateStageRequest request, User currentUser) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, pipeline.getCompany().getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));

        int orderIndex = request.getOrderIndex() != null
                ? request.getOrderIndex()
                : pipelineStageRepository.findByPipelineOrderByOrderIndex(pipeline).size();

        PipelineStage stage = createStage(pipeline, request.getName(), request.getType(), orderIndex);
        pipelineStageRepository.save(stage);
        return toStageResponse(stage);
    }

    @Transactional
    public List<PipelineStageResponse> reorderStages(UUID pipelineId, ReorderStagesRequest request, User currentUser) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found"));
        CompanyMember member = rbacService.requireActiveCompanyMember(currentUser, pipeline.getCompany().getId());
        rbacService.requireAnyRole(member, java.util.Set.of("SuperAdmin", "Admin"));

        List<UUID> stageIds = request.getStageIds();
        if (stageIds == null || stageIds.isEmpty()) {
            return pipelineStageRepository.findByPipelineOrderByOrderIndex(pipeline).stream()
                    .map(this::toStageResponse)
                    .toList();
        }

        List<PipelineStage> stages = pipelineStageRepository.findAllById(stageIds);
        for (PipelineStage s : stages) {
            if (!s.getPipeline().getId().equals(pipelineId)) {
                throw new IllegalArgumentException("Stage does not belong to this pipeline");
            }
        }

        for (int i = 0; i < stageIds.size(); i++) {
            UUID id = stageIds.get(i);
            PipelineStage stage = stages.stream().filter(s -> s.getId().equals(id)).findFirst().orElse(null);
            if (stage != null) {
                stage.setOrderIndex(i);
                pipelineStageRepository.save(stage);
            }
        }

        return pipelineStageRepository.findByPipelineOrderByOrderIndex(pipeline).stream()
                .map(this::toStageResponse)
                .toList();
    }

    private PipelineStageResponse toStageResponse(PipelineStage s) {
        PipelineStageResponse r = new PipelineStageResponse();
        r.setId(s.getId());
        r.setPipelineId(s.getPipeline().getId());
        r.setName(s.getName());
        r.setType(s.getType());
        r.setOrderIndex(s.getOrderIndex());
        return r;
    }
}

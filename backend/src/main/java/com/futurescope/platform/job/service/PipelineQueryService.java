package com.futurescope.platform.job.service;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.service.RbacService;
import com.futurescope.platform.job.domain.Pipeline;
import com.futurescope.platform.job.domain.PipelineStage;
import com.futurescope.platform.job.repository.PipelineRepository;
import com.futurescope.platform.job.repository.PipelineStageRepository;
import com.futurescope.platform.job.web.dto.PipelineResponse;
import com.futurescope.platform.job.web.dto.PipelineStageResponse;
import com.futurescope.platform.auth.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PipelineQueryService {

    private final CompanyRepository companyRepository;
    private final PipelineRepository pipelineRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final RbacService rbacService;

    public PipelineQueryService(CompanyRepository companyRepository,
                                 PipelineRepository pipelineRepository,
                                 PipelineStageRepository pipelineStageRepository,
                                 RbacService rbacService) {
        this.companyRepository = companyRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.rbacService = rbacService;
    }

    @Transactional(readOnly = true)
    public List<PipelineResponse> listByCompany(UUID companyId, User currentUser) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
        rbacService.requireActiveCompanyMember(currentUser, company.getId());
        return pipelineRepository.findByCompany(company).stream()
                .map(this::toPipelineResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PipelineStageResponse> getStages(UUID pipelineId, User currentUser) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline not found"));
        rbacService.requireActiveCompanyMember(currentUser, pipeline.getCompany().getId());
        return pipelineStageRepository.findByPipelineOrderByOrderIndex(pipeline).stream()
                .map(this::toStageResponse)
                .toList();
    }

    private PipelineResponse toPipelineResponse(Pipeline p) {
        PipelineResponse r = new PipelineResponse();
        r.setId(p.getId());
        r.setCompanyId(p.getCompany().getId());
        r.setName(p.getName());
        r.setDefault(p.isDefault());
        return r;
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

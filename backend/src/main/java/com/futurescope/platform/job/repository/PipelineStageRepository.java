package com.futurescope.platform.job.repository;

import com.futurescope.platform.job.domain.Pipeline;
import com.futurescope.platform.job.domain.PipelineStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PipelineStageRepository extends JpaRepository<PipelineStage, UUID> {

    List<PipelineStage> findByPipelineOrderByOrderIndex(Pipeline pipeline);

    Optional<PipelineStage> findByPipelineAndType(Pipeline pipeline, String type);
}

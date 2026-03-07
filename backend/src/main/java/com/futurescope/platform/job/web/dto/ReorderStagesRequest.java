package com.futurescope.platform.job.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class ReorderStagesRequest {

    @NotNull
    private List<UUID> stageIds;

    public List<UUID> getStageIds() {
        return stageIds;
    }

    public void setStageIds(List<UUID> stageIds) {
        this.stageIds = stageIds;
    }
}

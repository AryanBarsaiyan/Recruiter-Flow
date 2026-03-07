package com.futurescope.platform.job.web.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateStageRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private Integer orderIndex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}

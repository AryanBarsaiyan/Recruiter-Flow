package com.futurescope.platform.schedule.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class BookSlotRequest {

    @NotNull
    private OffsetDateTime scheduledStartAt;

    @NotNull
    private OffsetDateTime scheduledEndAt;

    public OffsetDateTime getScheduledStartAt() {
        return scheduledStartAt;
    }

    public void setScheduledStartAt(OffsetDateTime scheduledStartAt) {
        this.scheduledStartAt = scheduledStartAt;
    }

    public OffsetDateTime getScheduledEndAt() {
        return scheduledEndAt;
    }

    public void setScheduledEndAt(OffsetDateTime scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }
}

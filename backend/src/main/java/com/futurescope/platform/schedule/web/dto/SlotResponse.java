package com.futurescope.platform.schedule.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SlotResponse {

    private UUID id;
    private OffsetDateTime scheduledStartAt;
    private OffsetDateTime scheduledEndAt;
    private OffsetDateTime bookedByCandidateAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public OffsetDateTime getBookedByCandidateAt() {
        return bookedByCandidateAt;
    }

    public void setBookedByCandidateAt(OffsetDateTime bookedByCandidateAt) {
        this.bookedByCandidateAt = bookedByCandidateAt;
    }
}

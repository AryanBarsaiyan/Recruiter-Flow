package com.futurescope.platform.proctoring.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "proctoring_events")
public class ProctoringEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proctoring_session_id", nullable = false)
    private ProctoringSession proctoringSession;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "details")
    @JdbcTypeCode(SqlTypes.JSON)
    private String detailsJson;

    @Column(name = "weight", precision = 5, scale = 2)
    private BigDecimal weight;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProctoringSession getProctoringSession() { return proctoringSession; }
    public void setProctoringSession(ProctoringSession proctoringSession) { this.proctoringSession = proctoringSession; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(OffsetDateTime occurredAt) { this.occurredAt = occurredAt; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
}

package com.futurescope.platform.application.domain;

import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.domain.Resume;
import com.futurescope.platform.job.domain.Job;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_applications")
public class JobApplication {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "status", nullable = false, length = 64)
    private String status;

    @Column(name = "current_stage_id")
    private UUID currentStageId;

    @Column(name = "applied_at", nullable = false)
    private OffsetDateTime appliedAt;

    @Column(name = "last_status_at", nullable = false)
    private OffsetDateTime lastStatusAt;

    @Column(name = "source", length = 64)
    private String source;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getCurrentStageId() {
        return currentStageId;
    }

    public void setCurrentStageId(UUID currentStageId) {
        this.currentStageId = currentStageId;
    }

    public OffsetDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(OffsetDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public OffsetDateTime getLastStatusAt() {
        return lastStatusAt;
    }

    public void setLastStatusAt(OffsetDateTime lastStatusAt) {
        this.lastStatusAt = lastStatusAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}


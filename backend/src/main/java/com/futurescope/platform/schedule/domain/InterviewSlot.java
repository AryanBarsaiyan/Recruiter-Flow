package com.futurescope.platform.schedule.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "interview_slots")
public class InterviewSlot {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false)
    private InterviewInvitation invitation;

    @Column(name = "scheduled_start_at", nullable = false)
    private OffsetDateTime scheduledStartAt;

    @Column(name = "scheduled_end_at", nullable = false)
    private OffsetDateTime scheduledEndAt;

    @Column(name = "booked_by_candidate_at")
    private OffsetDateTime bookedByCandidateAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancelled_by", length = 32)
    private String cancelledBy;

    @Column(name = "reschedule_count", nullable = false)
    private int rescheduleCount;

    @Column(name = "no_show_candidate", nullable = false)
    private boolean noShowCandidate;

    @Column(name = "no_show_recruiter", nullable = false)
    private boolean noShowRecruiter;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InterviewInvitation getInvitation() {
        return invitation;
    }

    public void setInvitation(InterviewInvitation invitation) {
        this.invitation = invitation;
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

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(OffsetDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }

    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public int getRescheduleCount() {
        return rescheduleCount;
    }

    public void setRescheduleCount(int rescheduleCount) {
        this.rescheduleCount = rescheduleCount;
    }

    public boolean isNoShowCandidate() {
        return noShowCandidate;
    }

    public void setNoShowCandidate(boolean noShowCandidate) {
        this.noShowCandidate = noShowCandidate;
    }

    public boolean isNoShowRecruiter() {
        return noShowRecruiter;
    }

    public void setNoShowRecruiter(boolean noShowRecruiter) {
        this.noShowRecruiter = noShowRecruiter;
    }
}

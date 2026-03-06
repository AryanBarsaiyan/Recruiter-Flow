package com.futurescope.platform.ai.screening.service;

import com.futurescope.platform.ai.screening.client.AiResumeScreeningClient;
import com.futurescope.platform.ai.screening.client.dto.ResumeScreeningRequest;
import com.futurescope.platform.ai.screening.client.dto.ResumeScreeningResult;
import com.futurescope.platform.ai.screening.domain.ResumeScreening;
import com.futurescope.platform.ai.screening.repository.ResumeScreeningRepository;
import com.futurescope.platform.application.domain.ApplicationStageProgress;
import com.futurescope.platform.application.domain.JobApplication;
import com.futurescope.platform.application.repository.ApplicationStageProgressRepository;
import com.futurescope.platform.application.repository.JobApplicationRepository;
import com.futurescope.platform.candidate.domain.Resume;
import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.domain.Pipeline;
import com.futurescope.platform.job.domain.PipelineStage;
import com.futurescope.platform.job.repository.PipelineStageRepository;
import com.futurescope.platform.notification.service.NotificationService;
import com.futurescope.platform.schedule.domain.InterviewInvitation;
import com.futurescope.platform.schedule.repository.InterviewInvitationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ResumeScreeningService {

    private static final String RESULT_SHORTLISTED = "shortlisted";
    private static final String TYPE_AI_INTERVIEW = "ai_interview";
    private static final String TYPE_RESUME_SCREENING = "resume_screening";

    private final AiResumeScreeningClient client;
    private final ResumeScreeningRepository resumeScreeningRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewInvitationRepository interviewInvitationRepository;
    private final PipelineStageRepository pipelineStageRepository;
    private final ApplicationStageProgressRepository applicationStageProgressRepository;
    private final NotificationService notificationService;
    private final String appBaseUrl;

    public ResumeScreeningService(
            AiResumeScreeningClient client,
            ResumeScreeningRepository resumeScreeningRepository,
            JobApplicationRepository jobApplicationRepository,
            InterviewInvitationRepository interviewInvitationRepository,
            PipelineStageRepository pipelineStageRepository,
            ApplicationStageProgressRepository applicationStageProgressRepository,
            NotificationService notificationService,
            @Value("${app.base-url:https://app.example.com}") String appBaseUrl
    ) {
        this.client = client;
        this.resumeScreeningRepository = resumeScreeningRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewInvitationRepository = interviewInvitationRepository;
        this.pipelineStageRepository = pipelineStageRepository;
        this.applicationStageProgressRepository = applicationStageProgressRepository;
        this.notificationService = notificationService;
        this.appBaseUrl = appBaseUrl;
    }

    @Transactional
    public ResumeScreening screenApplication(JobApplication application) {
        Job job = application.getJob();
        Resume resume = application.getResume();

        ResumeScreeningRequest request = new ResumeScreeningRequest();
        request.setResumeText(resume.getParsedText());
        request.setResumeMetadataJson(resume.getParsedMetadataJson());
        request.setCriteriaJson(job.getResumeCriteriaJson());

        ResumeScreeningResult result = client.screen(request);

        ResumeScreening screening = new ResumeScreening();
        screening.setId(UUID.randomUUID());
        screening.setApplication(application);
        screening.setJob(job);
        screening.setResume(resume);
        screening.setMatchScore(result.getMatchScore());
        screening.setResult(result.getResult());
        screening.setExplanationJson(result.getExplanationJson());
        screening.setCriteriaSnapshotJson(job.getResumeCriteriaJson());
        screening.setCreatedAt(OffsetDateTime.now());
        resumeScreeningRepository.save(screening);

        OffsetDateTime now = OffsetDateTime.now();
        Pipeline pipeline = job.getPipeline();

        if (RESULT_SHORTLISTED.equalsIgnoreCase(result.getResult())) {
            application.setStatus("invited");
            application.setLastStatusAt(now);

            InterviewInvitation invitation = new InterviewInvitation();
            invitation.setId(UUID.randomUUID());
            invitation.setApplication(application);
            invitation.setInterviewType("ai_dsa");
            invitation.setToken(UUID.randomUUID().toString().replace("-", ""));
            invitation.setExpiresAt(now.plus(7, ChronoUnit.DAYS));
            invitation.setStatus("pending");
            invitation.setCreatedAt(now);
            interviewInvitationRepository.save(invitation);
            String inviteLink = appBaseUrl + "/interview-invitations/" + invitation.getToken();
            notificationService.sendInterviewInviteEmail(job.getCompany(), application.getCandidate().getUser(), inviteLink);

            if (pipeline != null) {
                pipelineStageRepository.findByPipelineAndType(pipeline, TYPE_RESUME_SCREENING).ifPresent(rsStage -> {
                    ApplicationStageProgress progress = new ApplicationStageProgress();
                    progress.setId(UUID.randomUUID());
                    progress.setApplication(application);
                    progress.setStage(rsStage);
                    progress.setStatus("passed");
                    progress.setStartedAt(now);
                    progress.setCompletedAt(now);
                    applicationStageProgressRepository.save(progress);
                });
                pipelineStageRepository.findByPipelineAndType(pipeline, TYPE_AI_INTERVIEW).ifPresent(aiStage -> {
                    application.setCurrentStageId(aiStage.getId());
                    ApplicationStageProgress progress = new ApplicationStageProgress();
                    progress.setId(UUID.randomUUID());
                    progress.setApplication(application);
                    progress.setStage(aiStage);
                    progress.setStatus("pending");
                    progress.setStartedAt(now);
                    applicationStageProgressRepository.save(progress);
                });
            }
        } else {
            application.setStatus("screened");
            application.setLastStatusAt(now);
            if (pipeline != null) {
                pipelineStageRepository.findByPipelineAndType(pipeline, TYPE_RESUME_SCREENING).ifPresent(rsStage -> {
                    ApplicationStageProgress progress = new ApplicationStageProgress();
                    progress.setId(UUID.randomUUID());
                    progress.setApplication(application);
                    progress.setStage(rsStage);
                    progress.setStatus("failed");
                    progress.setStartedAt(now);
                    progress.setCompletedAt(now);
                    applicationStageProgressRepository.save(progress);
                });
            }
        }

        jobApplicationRepository.save(application);
        return screening;
    }
}


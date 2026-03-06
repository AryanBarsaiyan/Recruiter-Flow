package com.futurescope.platform.application.repository;

import com.futurescope.platform.application.domain.ApplicationStageProgress;
import com.futurescope.platform.application.domain.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApplicationStageProgressRepository extends JpaRepository<ApplicationStageProgress, UUID> {

    List<ApplicationStageProgress> findByApplication(JobApplication application);
}

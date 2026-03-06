package com.futurescope.platform.job.repository;

import com.futurescope.platform.job.domain.Job;
import com.futurescope.platform.job.domain.JobCustomField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobCustomFieldRepository extends JpaRepository<JobCustomField, UUID> {

    List<JobCustomField> findByJob(Job job);

}


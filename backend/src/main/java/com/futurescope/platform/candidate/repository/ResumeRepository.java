package com.futurescope.platform.candidate.repository;

import com.futurescope.platform.candidate.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
}


package com.futurescope.platform.application.repository;

import com.futurescope.platform.application.domain.ApplicationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicationAnswerRepository extends JpaRepository<ApplicationAnswer, UUID> {
}


package com.futurescope.platform.interview.repository;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.interview.domain.QuestionBankQuestion;
import com.futurescope.platform.job.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionBankQuestionRepository extends JpaRepository<QuestionBankQuestion, UUID> {

    List<QuestionBankQuestion> findByCompanyAndActiveTrue(Company company);

    List<QuestionBankQuestion> findByJobAndActiveTrue(Job job);
}

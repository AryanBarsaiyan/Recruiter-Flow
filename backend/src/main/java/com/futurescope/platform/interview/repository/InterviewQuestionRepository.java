package com.futurescope.platform.interview.repository;

import com.futurescope.platform.interview.domain.Interview;
import com.futurescope.platform.interview.domain.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {

    List<InterviewQuestion> findByInterviewOrderBySequenceNumber(Interview interview);
}

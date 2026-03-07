package com.futurescope.platform.config;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.candidate.domain.Candidate;
import com.futurescope.platform.candidate.repository.CandidateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Seeds a test candidate for E2E tests when app.e2e-seed.enabled=true.
 * Creates test-candidate@example.com / password123 if not exists.
 */
@Component
@ConditionalOnProperty(name = "app.e2e-seed.enabled", havingValue = "true")
public class E2ECandidateSeeder {

    private static final Logger log = LoggerFactory.getLogger(E2ECandidateSeeder.class);
    private static final String DEFAULT_EMAIL = "test-candidate@example.com";
    private static final String DEFAULT_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    public E2ECandidateSeeder(UserRepository userRepository,
                              CandidateRepository candidateRepository,
                              PasswordEncoder passwordEncoder,
                              Environment env) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        String email = env.getProperty("app.e2e-seed.candidate-email", DEFAULT_EMAIL);
        String password = env.getProperty("app.e2e-seed.candidate-password", DEFAULT_PASSWORD);

        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            log.info("E2E test candidate already exists: {}", email);
            return;
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setUserType("candidate");
        user.setActive(true);
        user.setCreatedAt(OffsetDateTime.now());
        user = userRepository.save(user);

        Candidate candidate = new Candidate();
        candidate.setId(UUID.randomUUID());
        candidate.setUser(user);
        candidate.setFullName("E2E Test Candidate");
        candidate.setCreatedAt(OffsetDateTime.now());
        candidateRepository.save(candidate);

        log.info("E2E test candidate created: {} (password: {})", email, password);
    }
}

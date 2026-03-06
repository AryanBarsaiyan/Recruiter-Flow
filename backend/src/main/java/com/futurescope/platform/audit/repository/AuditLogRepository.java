package com.futurescope.platform.audit.repository;

import com.futurescope.platform.audit.domain.AuditLog;
import com.futurescope.platform.auth.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByCompanyOrderByCreatedAtDesc(Company company, Pageable pageable);
}

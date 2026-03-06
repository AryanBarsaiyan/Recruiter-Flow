package com.futurescope.platform.bulkimport.repository;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.bulkimport.domain.BulkImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BulkImportBatchRepository extends JpaRepository<BulkImportBatch, UUID> {

    List<BulkImportBatch> findByCompanyOrderByCreatedAtDesc(Company company);
}

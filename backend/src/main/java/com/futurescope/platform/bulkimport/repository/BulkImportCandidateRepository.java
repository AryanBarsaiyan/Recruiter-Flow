package com.futurescope.platform.bulkimport.repository;

import com.futurescope.platform.bulkimport.domain.BulkImportBatch;
import com.futurescope.platform.bulkimport.domain.BulkImportCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BulkImportCandidateRepository extends JpaRepository<BulkImportCandidate, UUID> {

    long countByBatch(BulkImportBatch batch);
}

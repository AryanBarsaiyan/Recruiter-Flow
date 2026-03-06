package com.futurescope.platform.auth.repository;

import com.futurescope.platform.auth.domain.Company;
import com.futurescope.platform.auth.domain.CompanyMember;
import com.futurescope.platform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {

    Optional<CompanyMember> findByCompanyAndUser(Company company, User user);

    List<CompanyMember> findByUser(User user);

    List<CompanyMember> findByUserAndStatusOrderByCreatedAtDesc(User user, String status);

    List<CompanyMember> findByCompanyOrderByCreatedAtDesc(Company company);

    @Query("""
            select cm from CompanyMember cm
            where cm.company.id = :companyId
              and cm.user.id = :userId
              and cm.status = :status
            """)
    Optional<CompanyMember> findActiveMembership(
            @Param("companyId") UUID companyId,
            @Param("userId") UUID userId,
            @Param("status") String status
    );

}


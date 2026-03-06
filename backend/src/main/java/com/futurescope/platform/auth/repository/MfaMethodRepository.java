package com.futurescope.platform.auth.repository;

import com.futurescope.platform.auth.domain.MfaMethod;
import com.futurescope.platform.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MfaMethodRepository extends JpaRepository<MfaMethod, UUID> {

    List<MfaMethod> findByUser(User user);

}


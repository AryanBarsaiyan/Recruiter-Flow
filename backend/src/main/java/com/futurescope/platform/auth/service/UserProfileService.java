package com.futurescope.platform.auth.service;

import com.futurescope.platform.auth.domain.User;
import com.futurescope.platform.auth.repository.UserRepository;
import com.futurescope.platform.auth.web.dto.UserProfileUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User updateProfile(User currentUser, UserProfileUpdateRequest request) {
        if (request.getFullName() != null) {
            currentUser.setFullName(request.getFullName().trim().isEmpty() ? null : request.getFullName().trim());
        }
        return userRepository.save(currentUser);
    }
}

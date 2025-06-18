package com.inertia.chat.modules.users.services.impl;

import com.inertia.chat.common.exceptions.ValidationException;
import com.inertia.chat.modules.users.dto.DeleteProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateStatusDTO;
import com.inertia.chat.modules.users.dto.UserListDTO;
import com.inertia.chat.modules.users.dto.UserProfileDTO;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.enums.UserStatus;
import com.inertia.chat.modules.users.repositories.UserRepository;
import com.inertia.chat.modules.users.services.UserService;
import com.inertia.chat.modules.chat.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorage fileStorage;

    @Override
    @Transactional(readOnly = true)
    public List<UserListDTO> getAllUsersExceptCurrent(User currentUser) {
        log.info("Fetching all users except user with id: {}", currentUser.getId());
        return userRepository.findAllByIdNot(currentUser.getId())
                .stream()
                .map(u -> new UserListDTO(
                    u.getId(),
                    u.getUsername(),
                    u.getName(),
                    u.getStatus(),
                    u.getLastSeen(),
                    u.getProfilePicture()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getProfile(User currentUser) {
        log.info("Fetching profile for user with id: {}", currentUser.getId());
        return new UserProfileDTO(
            currentUser.getId(),
            currentUser.getUsername(),
            currentUser.getName(),
            currentUser.getEmail(),
            currentUser.getProfilePicture(),
            currentUser.getStatus(),
            currentUser.getLastSeen()
        );
    }

    @Override
    @Transactional
    public UserListDTO updateProfileData(User currentUser, UpdateProfileDTO updateProfileDTO) {
        log.info("Updating profile data for user with id: {}", currentUser.getId());

        // Check if username is already taken by another user
        if (!currentUser.getUsername().equals(updateProfileDTO.getUsername()) &&
            userRepository.existsByUsername(updateProfileDTO.getUsername())) {
            log.warn("Username {} is already taken", updateProfileDTO.getUsername());
            throw new ValidationException("username", "Username is already taken");
        }

        currentUser.setName(updateProfileDTO.getName());
        currentUser.setUsername(updateProfileDTO.getUsername());

        User updatedUser = userRepository.save(currentUser);
        log.info("Profile data updated successfully for user with id: {}", currentUser.getId());

        return new UserListDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getName(),
            updatedUser.getStatus(),
            updatedUser.getLastSeen(),
            updatedUser.getProfilePicture()
        );
    }

    @Override
    @Transactional
    public UserListDTO updateAvatar(User currentUser, MultipartFile avatar) {
        log.info("Updating avatar for user with id: {}", currentUser.getId());

        // Validate file
        if (avatar == null || avatar.isEmpty()) {
            log.warn("Avatar file is empty for user with id: {}", currentUser.getId());
            throw new ValidationException("avatar", "Avatar file cannot be empty");
        }

        // Validate file size (max 1MB)
        if (avatar.getSize() > 1 * 1024 * 1024) {
            log.warn("Avatar file size exceeds limit for user with id: {}", currentUser.getId());
            throw new ValidationException("avatar", "Avatar file size must be less than 1MB");
        }

        // Validate content type
        String contentType = avatar.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Invalid avatar content type for user with id: {}", currentUser.getId());
            throw new ValidationException("avatar", "Avatar must be an image file");
        }

        String avatarUrl = fileStorage.uploadAvatar(avatar);
        currentUser.setProfilePicture(avatarUrl);

        User updatedUser = userRepository.save(currentUser);
        log.info("Avatar updated successfully for user with id: {}", currentUser.getId());

        return new UserListDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getName(),
            updatedUser.getStatus(),
            updatedUser.getLastSeen(),
            updatedUser.getProfilePicture()
        );
    }

    @Override
    @Transactional
    public void deleteProfile(User currentUser, DeleteProfileDTO deleteProfileDTO) {
        log.info("Attempting to delete profile for user with id: {}", currentUser.getId());

        // Verify password before deletion
        if (!passwordEncoder.matches(deleteProfileDTO.getPassword(), currentUser.getPassword())) {
            log.warn("Invalid password provided for user deletion, user id: {}", currentUser.getId());
            throw new RuntimeException("Invalid password");
        }

        userRepository.delete(currentUser);
        log.info("Profile deleted successfully for user with id: {}", currentUser.getId());
    }

    @Override
    @Transactional
    public UserListDTO updateStatus(User currentUser, UpdateStatusDTO updateStatusDTO) {
        log.info("Updating status for user with id: {}", currentUser.getId());

        // Only allow ONLINE and OFFLINE status
        if (updateStatusDTO.getStatus() != UserStatus.ONLINE && 
            updateStatusDTO.getStatus() != UserStatus.OFFLINE) {
            throw new ValidationException("status", "Invalid status. Only ONLINE and OFFLINE are allowed.");
        }

        // Use setUserStatus to ensure lastSeen is updated
        setUserStatus(currentUser, updateStatusDTO.getStatus());
        
        return new UserListDTO(
            currentUser.getId(),
            currentUser.getUsername(),
            currentUser.getName(),
            currentUser.getStatus(),
            currentUser.getLastSeen(),
            currentUser.getProfilePicture()
        );
    }

    @Override
    @Transactional
    public void setUserStatus(User user, UserStatus status) {
        log.info("Setting status {} for user with id: {}", status, user.getId());
        user.setStatus(status);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
        log.info("Status and last seen updated for user with id: {}", user.getId());
    }
}
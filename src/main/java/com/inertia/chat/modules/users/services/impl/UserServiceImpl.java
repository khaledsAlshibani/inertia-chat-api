package com.inertia.chat.modules.users.services.impl;

import com.inertia.chat.modules.users.dto.DeleteProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateStatusDTO;
import com.inertia.chat.modules.users.dto.UserListDTO;
import com.inertia.chat.modules.users.dto.UserProfileDTO;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.enums.UserStatus;
import com.inertia.chat.modules.users.repositories.UserRepository;
import com.inertia.chat.modules.users.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserListDTO> getAllUsersExceptCurrent(User currentUser) {
        log.info("Fetching all users except user with id: {}", currentUser.getId());
        return userRepository.findAllByIdNot(currentUser.getId())
                .stream()
                .map(u -> new UserListDTO(u.getId(), u.getUsername(), u.getName(), u.getStatus()))
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
    public UserListDTO updateProfile(User currentUser, UpdateProfileDTO updateProfileDTO) {
        log.info("Updating profile for user with id: {}", currentUser.getId());

        // Check if username is already taken by another user
        if (!currentUser.getUsername().equals(updateProfileDTO.getUsername()) &&
            userRepository.existsByUsername(updateProfileDTO.getUsername())) {
            log.warn("Username {} is already taken", updateProfileDTO.getUsername());
            throw new RuntimeException("Username is already taken");
        }

        // Check if email is already taken by another user
        if (!currentUser.getEmail().equals(updateProfileDTO.getEmail()) &&
            userRepository.existsByEmail(updateProfileDTO.getEmail())) {
            log.warn("Email {} is already taken", updateProfileDTO.getEmail());
            throw new RuntimeException("Email is already taken");
        }

        currentUser.setName(updateProfileDTO.getName());
        currentUser.setEmail(updateProfileDTO.getEmail());
        currentUser.setUsername(updateProfileDTO.getUsername());
        currentUser.setProfilePicture(updateProfileDTO.getProfilePicture());

        User updatedUser = userRepository.save(currentUser);
        log.info("Profile updated successfully for user with id: {}", currentUser.getId());

        return new UserListDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getName(),
            updatedUser.getStatus()
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
            throw new RuntimeException("Invalid status. Only ONLINE and OFFLINE are allowed.");
        }

        currentUser.setStatus(updateStatusDTO.getStatus());
        currentUser.setLastSeen(LocalDateTime.now());
        
        User updatedUser = userRepository.save(currentUser);
        log.info("Status updated successfully for user with id: {}", currentUser.getId());

        return new UserListDTO(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getName(),
            updatedUser.getStatus()
        );
    }

    @Override
    @Transactional
    public void setUserStatus(User user, UserStatus status) {
        log.info("Setting status {} for user with id: {}", status, user.getId());
        user.setStatus(status);
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }
}
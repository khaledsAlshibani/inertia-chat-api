package com.inertia.chat.modules.users.controllers;

import com.inertia.chat.common.dto.EnvelopeResponse;
import com.inertia.chat.modules.users.dto.DeleteProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateStatusDTO;
import com.inertia.chat.modules.users.dto.UserListDTO;
import com.inertia.chat.modules.users.dto.UserProfileDTO;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<EnvelopeResponse<List<UserListDTO>>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(EnvelopeResponse.success(
            userService.getAllUsersExceptCurrent(currentUser),
            "Users retrieved successfully"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<EnvelopeResponse<UserProfileDTO>> getProfile(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(EnvelopeResponse.success(
            userService.getProfile(currentUser),
            "Profile retrieved successfully"
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<EnvelopeResponse<UserListDTO>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        try {
            return ResponseEntity.ok(EnvelopeResponse.success(
                userService.updateProfile(currentUser, updateProfileDTO),
                "Profile updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(EnvelopeResponse.error(List.of(e.getMessage())));
        }
    }

    @PatchMapping("/me/status")
    public ResponseEntity<EnvelopeResponse<UserListDTO>> updateStatus(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateStatusDTO updateStatusDTO) {
        try {
            return ResponseEntity.ok(EnvelopeResponse.success(
                userService.updateStatus(currentUser, updateStatusDTO),
                "Status updated successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(EnvelopeResponse.error(List.of(e.getMessage())));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<EnvelopeResponse<Void>> deleteProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody DeleteProfileDTO deleteProfileDTO) {
        try {
            userService.deleteProfile(currentUser, deleteProfileDTO);
            return ResponseEntity.ok(EnvelopeResponse.success(
                null,
                "Profile deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(EnvelopeResponse.error(List.of(e.getMessage())));
        }
    }
}
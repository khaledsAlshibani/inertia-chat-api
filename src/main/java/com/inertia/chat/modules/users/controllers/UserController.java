package com.inertia.chat.modules.users.controllers;

import com.inertia.chat.common.dto.EnvelopeResponse;
import com.inertia.chat.modules.auth.dto.AuthResponse;
import com.inertia.chat.modules.auth.services.AuthService;
import com.inertia.chat.modules.auth.services.impl.AuthServiceImpl;
import com.inertia.chat.modules.auth.utils.CookieUtil;
import com.inertia.chat.modules.users.dto.DeleteProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateStatusDTO;
import com.inertia.chat.modules.users.dto.UserListDTO;
import com.inertia.chat.modules.users.dto.UserProfileDTO;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

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

    @PutMapping(value = "/me", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<EnvelopeResponse<UserProfileDTO>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @RequestPart("name") String name,
            @RequestPart("username") String username,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        try {
            UpdateProfileDTO updateProfileDTO = new UpdateProfileDTO();
            updateProfileDTO.setName(name);
            updateProfileDTO.setUsername(username);
            
            userService.updateProfile(currentUser, updateProfileDTO, avatar);
            UserProfileDTO updatedProfile = userService.getProfile(currentUser);
            
            return ResponseEntity.ok(EnvelopeResponse.success(
                updatedProfile,
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

    @PostMapping("/me/delete")
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
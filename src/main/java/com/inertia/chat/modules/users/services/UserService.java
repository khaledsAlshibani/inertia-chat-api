package com.inertia.chat.modules.users.services;

import com.inertia.chat.modules.users.dto.DeleteProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateProfileDTO;
import com.inertia.chat.modules.users.dto.UpdateStatusDTO;
import com.inertia.chat.modules.users.dto.UserListDTO;
import com.inertia.chat.modules.users.dto.UserProfileDTO;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.enums.UserStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    List<UserListDTO> getAllUsersExceptCurrent(User currentUser);
    UserProfileDTO getProfile(User currentUser);
    UserListDTO updateProfileData(User currentUser, UpdateProfileDTO updateProfileDTO);
    UserListDTO updateAvatar(User currentUser, MultipartFile avatar);
    void deleteProfile(User currentUser, DeleteProfileDTO deleteProfileDTO);
    UserListDTO updateStatus(User currentUser, UpdateStatusDTO updateStatusDTO);
    void setUserStatus(User user, UserStatus status);
}
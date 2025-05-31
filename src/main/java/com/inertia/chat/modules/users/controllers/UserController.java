package com.inertia.chat.modules.users.controllers;

import com.inertia.chat.modules.users.dto.UserListDTO;
import com.inertia.chat.modules.users.entities.User;
import com.inertia.chat.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserListDTO>> getAllUsers(@AuthenticationPrincipal User currentUser) {
        List<UserListDTO> users = userRepository.findAllByIdNot(currentUser.getId())
                .stream()
                .map(u -> new UserListDTO(u.getId(), u.getUsername(), u.getName(), u.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
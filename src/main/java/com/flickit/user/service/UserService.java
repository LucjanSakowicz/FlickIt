package com.flickit.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.user.dto.CreateUserRequest;
import com.flickit.user.dto.UserDto;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDto create(CreateUserRequest request) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone already in use");
        }
        UserEntity user = objectMapper.convertValue(request, UserEntity.class);
        user.setId(UUID.randomUUID());
        user.setRating(0.0);
        user.setRatingCount(0);
        return objectMapper.convertValue(userRepository.save(user), UserDto.class);
    }

    public UserDto getById(UUID id) {
        return userRepository.findById(id)
                .map(entity -> objectMapper.convertValue(entity, UserDto.class))
                .orElse(null);
    }

    public UserDto create(CreateUserRequest request, String rawPassword) {
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone already in use");
        }
        UserEntity user = objectMapper.convertValue(request, UserEntity.class);
        user.setId(UUID.randomUUID());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRating(0.0);
        user.setRatingCount(0);
        return objectMapper.convertValue(userRepository.save(user), UserDto.class);
    }

    public UUID login(String phone, String password) {
        var user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user.getId();
    }
}

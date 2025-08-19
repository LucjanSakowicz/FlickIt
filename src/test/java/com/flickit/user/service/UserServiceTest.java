package com.flickit.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flickit.user.dto.CreateUserRequest;
import com.flickit.user.dto.UserDto;
import com.flickit.user.model.UserEntity;
import com.flickit.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @MockBean
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService userService;

    @Test
    void create_shouldThrowIfPhoneExists() {
        // given
        CreateUserRequest request = new CreateUserRequest();
        request.setPhone("123456789");
        when(userRepository.existsByPhone("123456789")).thenReturn(true);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.create(request));
        verify(userRepository).existsByPhone("123456789");
    }

    @Test
    void create_shouldSaveUser() {
        // given
        CreateUserRequest request = new CreateUserRequest();
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(new UserEntity());

        // when
        UserDto result = userService.create(request);

        // then
        assertNotNull(result);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void getById_shouldReturnUserDto() {
        // given
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.of(new UserEntity()));

        // when
        UserDto result = userService.getById(id);

        // then
        assertNotNull(result);
        verify(userRepository).findById(id);
    }

    @Test
    void login_shouldReturnUserIdIfCredentialsValid() {
        // given
        String phone = "123";
        String password = "pass";
        UserEntity user = new UserEntity();
        user.setPasswordHash(new BCryptPasswordEncoder().encode(password));
        user.setId(UUID.randomUUID());
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));

        // when
        UUID result = userService.login(phone, password);

        // then
        assertEquals(user.getId(), result);
        verify(userRepository).findByPhone(phone);
    }

    @Test
    void login_shouldThrowIfInvalidCredentials() {
        // given
        String phone = "123";
        String password = "pass";
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class, () -> userService.login(phone, password));
        verify(userRepository).findByPhone(phone);
    }
} 